package com.leonjr.ldo.database.handler;

import java.sql.Connection;
import java.sql.SQLException;

import com.leonjr.ldo.app.models.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

final class ConnectionHandler {

    private static HikariDataSource dataSource;

    private static byte MAX_CONNECTIONS = 10;
    private static byte MIN_IDLE_CONNECTIONS = 2;
    private static int IDLE_TIMEOUT = 30000;
    private static int MAX_LIFETIME = 60000;
    private static int CONNECTION_TIMEOUT = 10000;
    private static int KEEPALIVE_TIME = 30000;

    public static void startDataSource(DatabaseConfig config) {
        HikariConfig hcPollCfg = new HikariConfig();
        hcPollCfg.setJdbcUrl(config.getJdbcUrl());
        hcPollCfg.setUsername(config.getUser());
        hcPollCfg.setPassword(config.getPassword());
        hcPollCfg.addDataSourceProperty("cachePrepStmts", "true");
        hcPollCfg.addDataSourceProperty("prepStmtCacheSize", "250");
        hcPollCfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hcPollCfg.setMaximumPoolSize(MAX_CONNECTIONS);
        hcPollCfg.setMinimumIdle(MIN_IDLE_CONNECTIONS);
        hcPollCfg.setIdleTimeout(IDLE_TIMEOUT);
        hcPollCfg.setMaxLifetime(MAX_LIFETIME);
        hcPollCfg.setKeepaliveTime(KEEPALIVE_TIME);
        hcPollCfg.setConnectionTimeout(CONNECTION_TIMEOUT);
        dataSource = new HikariDataSource(hcPollCfg);
    }

    public static Connection getConnection() throws SQLException, RuntimeException {
        if (dataSource == null) {
            throw new RuntimeException("Data source not initialized");
        }
        return dataSource.getConnection();
    }

    public static void closeConnection(Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

}
