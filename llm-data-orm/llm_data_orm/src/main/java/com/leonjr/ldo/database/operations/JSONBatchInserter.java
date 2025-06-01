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
     * Inserts a JSON array into a database table using batch processing with
     * configurable chunk sizes.
     * This method processes the JSON array in chunks to optimize memory usage and
     * database performance.
     * 
     * <p>
     * The method supports automatic data type mapping from JSON values to
     * appropriate SQL types,
     * including special handling for ISO 8601 date/time strings. It also supports
     * truncating the
     * target table before insertion if configured to do so.
     * </p>
     * 
     * <p>
     * The insertion is performed within a transaction - if any rows are
     * successfully inserted,
     * the transaction is committed; otherwise, it is rolled back.
     * </p>
     * 
     * @param conn      the database connection to use for the insertion operation
     * @param tableDesc the table description containing metadata about the target
     *                  table structure
     * @param jsonArray the JSON array containing the data to be inserted
     * @param chunkSize the number of rows to process in each batch before executing
     *                  the SQL statement
     * @return true if any rows were successfully inserted, false otherwise
     * @throws SQLException if a database access error occurs during the insertion
     *                      process
     * @throws Exception    if an error occurs during JSON processing or other
     *                      operations
     * 
     * @see TableDescription
     * @see ColumnDescription
     * @see JsonNode
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

        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableName).append(" (");
        for (int i = 0; i < cols.size(); i++) {
            sql.append(cols.get(i).getName());
            if (i < cols.size() - 1)
                sql.append(", ");
        }
        sql.append(") VALUES (");
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
                if (count % chunkSize == 0) {
                    anyInserted |= ps.executeBatch().length > 0;
                }
            }
            if (count % chunkSize != 0) {
                anyInserted |= ps.executeBatch().length > 0;
            }

            if (anyInserted) {
                conn.commit();
            } else {
                conn.rollback();
            }
        }

        return anyInserted;
    }

    /**
     * Checks if the given text string represents a valid ISO 8601 date-time format.
     * 
     * This method attempts to parse the input string using the ISO_OFFSET_DATE_TIME
     * formatter, which expects the format: yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX
     * (e.g., "2023-12-25T14:30:00+02:00").
     * 
     * @param text the string to be validated as ISO 8601 date-time format
     * @return true if the text is a valid ISO 8601 date-time string, false
     *         otherwise
     */
    private static boolean isIso8601(String text) {
        try {
            OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Truncates the specified table before performing batch insert operations.
     * This method removes all rows from the table while preserving the table
     * structure,
     * which is useful for clearing existing data before inserting new batch data.
     * 
     * @param conn      the database connection to use for the truncate operation
     * @param tableName the name of the table to truncate
     * @return true if the truncate operation affected at least one row, false
     *         otherwise
     * @throws SQLException if a database access error occurs or the table doesn't
     *                      exist
     */
    private static boolean truncateTableBeforeInsert(Connection conn, String tableName) throws SQLException {
        String sql = "TRUNCATE TABLE " + tableName;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate() > 0;
        }
    }
}