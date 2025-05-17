package com.leonjr.ldo.database.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.database.models.TableDescription;
import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.database.models.ColumnDescription;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
            int chunkSize) throws SQLException, Exception {
        boolean anyInserted = false;
        String tableName = tableDesc.getName();
        List<ColumnDescription> cols = tableDesc.getColumns();

        if (AppStore.getInstance().getStartupConfiguration().getDatabase().isTruncateTableBeforeInsert()) {
            truncateTableBeforeInsert(conn, tableName);
        }

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
                    String colType = cols.get(c).getType();
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
                    } else if (value.isTextual() && isIso8601(value.asText())) {
                        OffsetDateTime odt = OffsetDateTime.parse(value.asText());
                        if (colType.equals("DATE")) {
                            ps.setDate(c + 1, Date.valueOf(odt.toLocalDate()));
                        } else if (colType.equals("TIME")) {
                            ps.setTime(c + 1, Time.valueOf(odt.toLocalTime().withNano(0)));
                        } else if (colType.equals("TIMESTAMP")) {
                            ps.setTimestamp(c + 1, Timestamp.from(odt.toInstant()));
                        } else {
                            ps.setString(c + 1, value.asText());
                        }
                    } else if (value.isTextual()) {
                        ps.setString(c + 1, value.asText());
                    } else if (value.isBinary()) {
                        ps.setBytes(c + 1, value.binaryValue());
                    } else if (value.isArray() || value.isObject()) {
                        ps.setObject(c + 1, value.toString());
                    } else if (value.isBigInteger() || value.isBigDecimal()) {
                        ps.setBigDecimal(c + 1, value.decimalValue());
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

    private static boolean isIso8601(String text) {
        try {
            OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean truncateTableBeforeInsert(Connection conn, String tableName) throws SQLException {
        String sql = "TRUNCATE TABLE " + tableName;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate() > 0;
        }
    }
}