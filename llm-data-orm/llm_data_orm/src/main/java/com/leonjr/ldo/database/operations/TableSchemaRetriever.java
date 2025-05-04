package com.leonjr.ldo.database.operations;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.leonjr.ldo.app.enums.DatabaseType;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.database.models.ColumnDescription;
import com.leonjr.ldo.database.models.TableDescription;

public class TableSchemaRetriever {

    /**
     * Get table information from the database connection
     * 
     * @param conn      Connection object
     * @param tableName Table name to retrieve information
     * @param dbType    Database type (e.g. POSTGRES, MYSQL)
     * @return TableDescription object containing table information
     * @throws SQLException If an error occurs while retrieving the table
     *                      information
     */
    public static TableDescription getTableInfo(Connection conn, String tableName, DatabaseType dbType)
            throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        LoggerHelper.logger.info("Retrieving columns for table: " + tableName);

        String schema = null;
        if (dbType == DatabaseType.POSTGRES) {
            schema = "public";
        }

        var parsedColumns = new ArrayList<ColumnDescription>();

        try (ResultSet columns = metaData.getColumns(null, schema, tableName, null)) {

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                boolean isNullable = columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
                String defaultValue = columns.getString("COLUMN_DEF");

                // Handle PostgreSQL auto-increment (SERIAL type)
                if (dbType == DatabaseType.POSTGRES && "NO".equals(isAutoIncrement)) {
                    if (columnType.toLowerCase().contains("serial")) {
                        isAutoIncrement = "YES";
                    }
                }

                var columnDescription = ColumnDescription.builder()
                        .name(columnName)
                        .type(columnType)
                        .size(columnSize)
                        .nullable(isNullable)
                        .autoIncrement(isAutoIncrement)
                        .defaultValue(defaultValue)
                        .build();

                parsedColumns.add(columnDescription);
            }
        }

        LoggerHelper.logger.info(parsedColumns.size() + " columns found to " + tableName);

        return TableDescription.builder()
                .name(tableName)
                .columns(parsedColumns)
                .build();
    }
}
