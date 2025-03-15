package com.leonjr.ldo;

import java.util.Calendar;

import com.leonjr.ldo.app.consts.AppConsts;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.app.helper.YmlHelper;

public class Main {
    public static void main(String[] args) {
        boot();
    }

    public static void boot() {
        LoggerHelper.logger.info("Starting application at ", Calendar.getInstance().getTime());
        LoggerHelper.logger.info(AppConsts.APP_ASC_TITLE);
        LoggerHelper.logger.info("Loading configuration file...");
        var startupConf = YmlHelper.getStartupConfiguration();
        LoggerHelper.logger.info("Configuration loaded successfully!");
        LoggerHelper.logger.info(startupConf);
        var appStore = AppStore.getInstance(startupConf);
    }
}