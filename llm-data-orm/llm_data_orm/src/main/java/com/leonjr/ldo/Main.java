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

/**
 * Main entry point for the LLM Data ORM application.
 * This class implements a command-line interface using PicoCLI for processing data through an ETL pipeline.
 * 
 * The application supports various operations including:
 * - Loading configuration files
 * - Processing files or folders through ETL pipeline
 * - Database operations with specified table names
 * - Debug mode and test set validation
 * - Page-specific processing with regex patterns
 * 
 * Required parameters:
 * - Configuration file path (-c, --config)
 * - Table name (-t, --table)
 * 
 * Optional parameters:
 * - File or folder path to process (-f, --file, --folder)
 * - Execution flag to run ETL pipeline (-e, --exec)
 * - Debug mode (-d, --debug)
 * - Test set path (-ts, --testset)
 * - Pages regex for selective processing (-p, --pages)
 * - Help display (-h, --help)
 * 
 * The application follows a boot-then-execute pattern where configuration is loaded first,
 * then optionally runs the ETL pipeline if the execution flag is set.
 * 
 * @author leonjr
 * @version 1.0
 * @since 1.0
 */
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

    @Option(names = { "-p", "--pages" }, description = "Pages regex to process (if applicable). It can be used to represent a set of pages or a range of pages e.g., 1,2,3 or 1-3 ( comma separated or range). If not set, all pages will be processed.")
    private String pagesRegex;

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
        boot(configFilePath, tableName, debug, testSetPath, pagesRegex);
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
    public static void boot(String configFilePath, String tableName, boolean debug, String testSetPath, String pagesRegex) {
        LoggerHelper.logger.info("Starting application at ", Calendar.getInstance().getTime());
        LoggerHelper.logger.info(AppConsts.APP_ASC_TITLE);
        LoggerHelper.logger.info("Loading configuration file...");
        try {
            var startupConf = YmlHelper.getStartupConfiguration(configFilePath);
            if (testSetPath != null) {
                validateTestSetPath(testSetPath);
            }
            LoggerHelper.logger.info("Configuration loaded successfully!");
            LoggerHelper.logger.info(startupConf);
            AppStore.getInstance(startupConf, tableName, debug, testSetPath, pagesRegex);
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