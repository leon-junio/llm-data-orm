package com.leonjr.ldo.database.handler;

import java.sql.Connection;
import java.sql.SQLException;

import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.app.models.DatabaseConfig;
import com.leonjr.ldo.database.models.TableDescription;
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
    public static TableDescription getTableDescription(String tableName) {
        try (Connection connection = ConnectionHandler.getConnection()) {
            var dbType = AppStore.getInstance().getStartupConfiguration().getDatabase().getDatabaseType();
            return TableSchemaRetriever.getTableInfo(connection, tableName, dbType);
        } catch (SQLException e) {
            LoggerHelper.logger.error("Error while retrieving table description: " + e.getMessage());
            return null;
        } catch (Exception e) {
            LoggerHelper.logger.error("Something went wrong: " + e.getMessage());
            return null;
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
