package com.leonjr.ldo;

import java.util.Calendar;

import com.leonjr.ldo.app.consts.AppConsts;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.app.helper.YmlHelper;
import com.leonjr.ldo.database.handler.DBHelper;

public class Main {
    public static void main(String[] args) {
        boot();
        test();
    }

    public static void boot() {
        LoggerHelper.logger.info("Starting application at ", Calendar.getInstance().getTime());
        LoggerHelper.logger.info(AppConsts.APP_ASC_TITLE);
        LoggerHelper.logger.info("Loading configuration file...");
        var startupConf = YmlHelper.getStartupConfiguration();
        LoggerHelper.logger.info("Configuration loaded successfully!");
        LoggerHelper.logger.info(startupConf);
        AppStore.getInstance(startupConf);
        DBHelper.startDB(startupConf.getDatabase());
    }

    public static void test() {
        LoggerHelper.logger.info("Testing...");
        var tableDescription = DBHelper
                .getTableDescription(AppStore.getInstance().getStartupConfiguration().getApp().getTargetTableName());
        LoggerHelper.logger.info(tableDescription);
        LoggerHelper.logger.info("Table description in JSON format:");
        LoggerHelper.logger.info(tableDescription.toJson());
        LoggerHelper.logger.info("Test completed!");
        DBHelper.shutdown();
    }
}