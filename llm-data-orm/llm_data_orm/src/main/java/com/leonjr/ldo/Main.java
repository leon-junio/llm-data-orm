package com.leonjr.ldo;

import java.util.Calendar;
import java.util.concurrent.Callable;

import com.leonjr.ldo.app.consts.AppConsts;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.app.helper.YmlHelper;
import com.leonjr.ldo.database.handler.DBHelper;

import picocli.CommandLine;
import picocli.CommandLine.Option;

public class Main implements Callable<Integer> {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        LoggerHelper.logger.info("Shutting down application...");
        DBHelper.shutdown();
        System.exit(exitCode);
    }

    @Option(names = { "-c", "--config" }, description = "Path to the configuration file")
    private String configFilePath;

    @Option(names = { "-l", "--listen" }, description = "Listen for incoming inputs")
    private boolean listen;

    @Option(names = { "-t", "--test" }, description = "Run test")
    private boolean test;

    @Option(names = { "-tn", "--table-name" }, description = "Table name to retrieve information")
    private String tableName;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display a help message")
    private boolean helpRequested;

    @Option(names = { "-f", "--file", "--folder" }, description = "File or folder to process")
    private String fileOrFolderpath;

    @Override
    public Integer call() {
        if (helpRequested) {
            CommandLine.usage(this, System.out);
            return 0;
        }
        boot(configFilePath, tableName);
        if (listen) {
            LoggerHelper.logger.info("Listening for incoming inputs...");
        }
        if (test) {
            test();
        }
        return 0;
    }

    public static void boot(String configFilePath, String tableName) {
        LoggerHelper.logger.info("Starting application at ", Calendar.getInstance().getTime());
        LoggerHelper.logger.info(AppConsts.APP_ASC_TITLE);
        LoggerHelper.logger.info("Loading configuration file...");
        try {
            var startupConf = YmlHelper.getStartupConfiguration(configFilePath);
            LoggerHelper.logger.info("Configuration loaded successfully!");
            LoggerHelper.logger.info(startupConf);
            AppStore.getInstance(startupConf, tableName);
            DBHelper.startDB(startupConf.getDatabase());
            LoggerHelper.logger.info("Application started successfully!");
        } catch (Exception e) {
            LoggerHelper.logger.error("Error while starting application: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void test() {
        LoggerHelper.logger.info("Testing...");
        var tableDescription = DBHelper
                .getTableDescription(AppStore.getInstance().getTableName());
        LoggerHelper.logger.info(tableDescription);
        LoggerHelper.logger.info("Table description in JSON format:");
        LoggerHelper.logger.info(tableDescription.toJson());
        LoggerHelper.logger.info("Test completed!");
    }
}