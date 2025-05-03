package com.leonjr.ldo.database.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.database.models.TableDescription;
import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.database.models.ColumnDescription;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class JSONBatchInserter {

    /**
     * Insere em lotes (chunks) o JSON array na tabela descrita.
     *
     * @param conn      Conexão JDBC aberta
     * @param tableDesc Descrição da tabela (nome + colunas)
     * @param jsonArray JsonNode array com os objetos a inserir
     * @param chunkSize Quantidade de registros por lote
     * @throws SQLException
     */
    public static boolean insertJsonArrayInChunks(
            Connection conn,
            TableDescription tableDesc,
            JsonNode jsonArray,
            int chunkSize) throws SQLException {
        boolean anyInserted = false;
        String tableName = tableDesc.getName();
        List<ColumnDescription> cols = tableDesc.getColumns();

        // Monta a parte fixa do INSERT: "INSERT INTO table (col1, col2, ...) VALUES "
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableName).append(" (");
        for (int i = 0; i < cols.size(); i++) {
            sql.append(cols.get(i).getName());
            if (i < cols.size() - 1)
                sql.append(", ");
        }
        sql.append(") VALUES (");
        // placeholders (?,?,?,...)
        sql.append("?".repeat(Math.max(0, cols.size()))
                .replaceAll("(.)(?=.)", "$1, "));
        sql.append(")");

        if (AppStore.getInstance().isDebugAll()) {
            LoggerHelper.logger.debug("SQL: " + sql.toString());
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int count = 0;
            int totalRows = jsonArray.size();

            for (int idx = 0; idx < totalRows; idx++) {
                JsonNode row = jsonArray.get(idx);
                // Para cada coluna, extrai o valor do JSON e seta no PreparedStatement
                for (int c = 0; c < cols.size(); c++) {
                    String colName = cols.get(c).getName();
                    JsonNode value = row.get(colName);
                    if (value == null || value.isNull()) {
                        ps.setObject(c + 1, null);
                    } else if (value.isInt()) {
                        ps.setInt(c + 1, value.intValue());
                    } else if (value.isLong()) {
                        ps.setLong(c + 1, value.longValue());
                    } else if (value.isDouble() || value.isFloat()) {
                        ps.setDouble(c + 1, value.doubleValue());
                    } else if (value.isBoolean()) {
                        ps.setBoolean(c + 1, value.booleanValue());
                    } else {
                        ps.setString(c + 1, value.asText());
                    }
                }

                ps.addBatch();
                count++;

                // Quando atinge chunkSize, executa e limpa o batch
                if (count % chunkSize == 0) {
                    anyInserted |= ps.executeBatch().length > 0;
                }
            }

            // Finaliza o batch restante
            if (count % chunkSize != 0) {
                anyInserted |= ps.executeBatch().length > 0;
            }

            if (anyInserted) {
                conn.commit(); // Commit após inserção
            } else {
                conn.rollback(); // Rollback se nada foi inserido
            }
        }

        return anyInserted;
    }
}