package com.leonjr.ldo;

import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.leonjr.ldo.confs.helper.YmlHelper;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        boot();
    }

    public static void boot() {
        logger.info("Starting application at ", Calendar.getInstance().getTime());
        logger.info(
                "\n ___       ________  ________                             ___       ___       _____ ______           ________  ________  _____ ______      \r\n"
                        + //
                        "|\\  \\     |\\   ___ \\|\\   __  \\                           |\\  \\     |\\  \\     |\\   _ \\  _   \\        |\\   __  \\|\\   __  \\|\\   _ \\  _   \\    \r\n"
                        + //
                        "\\ \\  \\    \\ \\  \\_|\\ \\ \\  \\|\\  \\        ____________      \\ \\  \\    \\ \\  \\    \\ \\  \\\\\\__\\ \\  \\       \\ \\  \\|\\  \\ \\  \\|\\  \\ \\  \\\\\\__\\ \\  \\   \r\n"
                        + //
                        " \\ \\  \\    \\ \\  \\ \\\\ \\ \\  \\\\\\  \\      |\\____________\\     \\ \\  \\    \\ \\  \\    \\ \\  \\\\|__| \\  \\       \\ \\  \\\\\\  \\ \\   _  _\\ \\  \\\\|__| \\  \\  \r\n"
                        + //
                        "  \\ \\  \\____\\ \\  \\_\\\\ \\ \\  \\\\\\  \\     \\|____________|      \\ \\  \\____\\ \\  \\____\\ \\  \\    \\ \\  \\       \\ \\  \\\\\\  \\ \\  \\\\  \\\\ \\  \\    \\ \\  \\ \r\n"
                        + //
                        "   \\ \\_______\\ \\_______\\ \\_______\\                          \\ \\_______\\ \\_______\\ \\__\\    \\ \\__\\       \\ \\_______\\ \\__\\\\ _\\\\ \\__\\    \\ \\__\\\r\n"
                        + //
                        "    \\|_______|\\|_______|\\|_______|                           \\|_______|\\|_______|\\|__|     \\|__|        \\|_______|\\|__|\\|__|\\|__|     \\|__|\r\n"
                        + //
                        "                                                                                                                                           \r\n"
                        + //
                        "                                                                                                                                           \r\n"
                        + //
                        "                                                                                                                                           ");
        logger.info("Loading configuration file...");
        var startupConf = YmlHelper.getStartupConfiguration();
        logger.info("Configuration loaded successfully!");
        logger.info(startupConf);
    }
}