package com.leonjr.ldo.app.helper;

import com.leonjr.ldo.app.models.StartupConfiguration;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.yaml.snakeyaml.Yaml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class YmlHelper {

    /**
     * Get YAML configuration file and parse it to StartupConfiguration object
     * 
     * @param lodConfigPath Path to the configuration file
     * 
     * @return StartupConfiguration object with parsed configuration
     * @throws RuntimeException if configuration file is not found or invalid
     * @throws IOException      if an I/O error occurs while reading the file
     */
    public static StartupConfiguration getStartupConfiguration(String lodConfigPath)
            throws RuntimeException, IOException {
        Yaml yaml = new Yaml();
        StartupConfiguration config = null;

        try (var buffStream = new BufferedInputStream(new FileInputStream(lodConfigPath))) {
            config = yaml.loadAs(buffStream, StartupConfiguration.class);
        }

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