package com.leonjr.ldo;

import java.io.File;
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

    @Option(names = { "-t", "--table" }, description = "Table name to retrieve information")
    private String tableName;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display a help message")
    private boolean helpRequested;

    @Option(names = { "-f", "--file", "--folder" }, description = "File or folder to process")
    private String fileOrFolderpath;

    @Option(names = { "-e", "--exec" }, description = "Run execution process")
    private boolean exec;

    @Option(names = { "-d", "--debug" }, description = "Enable debug mode")
    private boolean debug;

    @Option(names = { "-ts", "--testset" }, description = "Path to the test set")
    private String testSetPath;

    @Override
    public Integer call() {
        if (helpRequested) {
            CommandLine.usage(this, System.out);
            return 0;
        }
        if (configFilePath == null || tableName == null) {
            LoggerHelper.logger.error("Configuration file path and table name are required!");
            return 1;
        }
        boot(configFilePath, tableName, debug, testSetPath);
        if (exec) {
            return startETLPipeline(fileOrFolderpath);
        }
        return 0;
    }

    /**
     * Boot the application with the provided configuration file path and table name
     * 
     * @param configFilePath Path to the configuration file
     * @param tableName      Table name to retrieve information
     */
    public static void boot(String configFilePath, String tableName, boolean debug, String testSetPath) {
        LoggerHelper.logger.info("Starting application at ", Calendar.getInstance().getTime());
        LoggerHelper.logger.info(AppConsts.APP_ASC_TITLE);
        LoggerHelper.logger.info("Loading configuration file...");
        try {
            var startupConf = YmlHelper.getStartupConfiguration(configFilePath);
            validateTestSetPath(testSetPath);
            LoggerHelper.logger.info("Configuration loaded successfully!");
            LoggerHelper.logger.info(startupConf);
            AppStore.getInstance(startupConf, tableName, debug, testSetPath);
            DBHelper.startDB(startupConf.getDatabase());
            LoggerHelper.logger.info("Application started successfully!");
        } catch (Exception e) {
            LoggerHelper.logger.error("Error while starting application: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Start the ETL pipeline to process the provided file or folder path
     * 
     * @param fileOrFolderPath File or folder path to process
     * @return 0 if the process finished successfully, 1 otherwise
     */
    public int startETLPipeline(String fileOrFolderPath) {
        try {
            LoggerHelper.logger.info("ETL Pipeline started to process file/folder: " + fileOrFolderPath);
            ETLPipeline etlPipeline = new ETLPipeline(fileOrFolderPath);
            etlPipeline.boot();
            LoggerHelper.logger.info("ETL Pipeline finished successfully!");
            return 0;
        } catch (Exception e) {
            LoggerHelper.logger.error("ETL Pipeline failed to execute: " + e.getMessage());
            LoggerHelper.logger.catching(e);
            return 1;
        }
    }

        /**
     * Validate the test set path.
     * 
     * @throws IllegalArgumentException if the test set path is not set or does not
     *                                  exist
     */
    private static void validateTestSetPath(String testSetPath) throws IllegalArgumentException {
        if (testSetPath == null || testSetPath.isEmpty()) {
            throw new IllegalArgumentException("Test set path is not set in the configuration.");
        }
        var file = new File(testSetPath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Test set path does not exist: " + testSetPath);
        }
    }
}