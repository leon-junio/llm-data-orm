package com.leonjr.ldo.database.handler;

import java.sql.Connection;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.app.models.DatabaseConfig;
import com.leonjr.ldo.database.models.TableDescription;
import com.leonjr.ldo.database.operations.JSONBatchInserter;
import com.leonjr.ldo.database.operations.TableSchemaRetriever;

public class DBHelper {

    private static DBHelper instance = null;

    public DBHelper getInstance() {
        if (instance == null) {
            instance = new DBHelper();
        }
        return instance;
    }

    /**
     * Start the database connection using the provided configuration
     * 
     * @param config Database configuration object
     */
    public static void startDB(DatabaseConfig config) {
        ConnectionHandler.startDataSource(config);
    }

    /**
     * Get the table description for the provided table name
     * 
     * @param tableName Table name to retrieve information
     * @return TableDescription object containing table information
     */
    public static TableDescription getTableDescription(String tableName) throws Exception {
        try (Connection connection = ConnectionHandler.getConnection()) {
            var dbType = AppStore.getStartConfigs().getDatabase().getDatabaseType();
            return TableSchemaRetriever.getTableInfo(connection, tableName, dbType);
        } catch (SQLException e) {
            LoggerHelper.logger.error("Error while retrieving table description: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LoggerHelper.logger.error("Something went wrong: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Insert a parsed JSON document into the database
     * 
     * @param tableName        Table name to insert the document
     * @param tableDescription Table description object
     * @param json             Parsed JSON document to insert
     * @return true if insertion was successful, false otherwise
     */
    public static boolean insertParsedDocumentAtDatabase(String tableName, TableDescription tableDescription,
            JsonNode json) {
        try (Connection connection = ConnectionHandler.getConnection()) {
            connection.setAutoCommit(false);
            return JSONBatchInserter.insertJsonArrayInChunks(connection, tableDescription, json,
                    AppStore.getStartConfigs().getApp().getMaxDBInsertionChunkSize());
        } catch (SQLException e) {
            LoggerHelper.logger.error("Error while inserting document: " + e.getMessage());
            return false;
        } catch (Exception e) {
            LoggerHelper.logger.error("Something went wrong: " + e.getMessage());
            return false;
        }
    }

    /**
     * Shutdown the database connection
     */
    public static void shutdown() {
        LoggerHelper.logger.info("Shutting down database connection...");
        ConnectionHandler.closeDataSource();
        LoggerHelper.logger.info("Database connection closed successfully!");
    }

}
