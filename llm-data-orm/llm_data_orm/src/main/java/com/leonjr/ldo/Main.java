package com.leonjr.ldo;

import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.leonjr.ldo.app.consts.AppConsts;
import com.leonjr.ldo.app.helper.YmlHelper;

public class Main {

    public static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        boot();
    }

    public static void boot() {
        LOGGER.info("Starting application at ", Calendar.getInstance().getTime());
        LOGGER.info(AppConsts.APP_ASC_TITLE);
        LOGGER.info("Loading configuration file...");
        var startupConf = YmlHelper.getStartupConfiguration();
        LOGGER.info("Configuration loaded successfully!");
        LOGGER.info(startupConf);
        var appStore = AppStore.getInstance(startupConf);
    }
}