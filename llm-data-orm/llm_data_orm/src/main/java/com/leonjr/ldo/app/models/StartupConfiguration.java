package com.leonjr.ldo.app.models;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StartupConfiguration {
    @NotNull(message = "App configuration is required")
    private AppConfig app;
    @NotNull(message = "Database configuration is required")
    private DatabaseConfig database;
}