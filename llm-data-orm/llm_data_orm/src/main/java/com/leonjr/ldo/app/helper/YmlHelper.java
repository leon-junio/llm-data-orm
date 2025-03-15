package com.leonjr.ldo.app.helper;

import com.leonjr.ldo.Main;
import com.leonjr.ldo.app.models.StartupConfiguration;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Set;

public class YmlHelper {

    /**
     * Get YAML configuration file and parse it to StartupConfiguration object
     * 
     * @return StartupConfiguration object with parsed configuration
     * @throws RuntimeException if configuration file is not found or invalid
     */
    public static StartupConfiguration getStartupConfiguration() throws RuntimeException {
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("app_config.yml");

        if (inputStream == null) {
            throw new RuntimeException("app_config.yml not found!");
        }

        StartupConfiguration config = yaml.loadAs(inputStream, StartupConfiguration.class);

        if (config == null) {
            throw new RuntimeException("Invalid configuration file format!");
        }

        validate(config);

        return config;
    }

    /**
     * Validate YAML configuration file
     * 
     * @param config StartupConfiguration object
     */
    private static void validate(StartupConfiguration config) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<StartupConfiguration>> violations = validator.validate(config);

        if (!violations.isEmpty()) {
            LoggerHelper.logger.warn("⚠️ Validation errors found:");
            for (ConstraintViolation<StartupConfiguration> violation : violations) {
                LoggerHelper.logger.error("❌ " + violation.getMessage());
            }
            throw new RuntimeException("Invalid configuration file!");
        } else {
            LoggerHelper.logger.info("✅ Config validated with success: " + config);
        }
    }

}