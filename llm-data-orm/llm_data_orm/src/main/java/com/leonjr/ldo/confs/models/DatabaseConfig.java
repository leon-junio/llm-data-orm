package com.leonjr.ldo.confs.models;

import com.leonjr.ldo.confs.enums.DatabaseType;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DatabaseConfig {
    private String host;
    private int port;
    private String user;
    private String password;
    private String databaseName;
    private DatabaseType databaseType;
}
