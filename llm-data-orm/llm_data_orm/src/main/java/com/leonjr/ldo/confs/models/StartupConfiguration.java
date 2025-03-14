package com.leonjr.ldo.confs.models;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StartupConfiguration {
    private AppConfig app;
    private DatabaseConfig database;
}