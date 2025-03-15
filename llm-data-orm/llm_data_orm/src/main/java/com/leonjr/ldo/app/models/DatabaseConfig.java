package com.leonjr.ldo.app.models;

import com.leonjr.ldo.app.enums.DatabaseType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DatabaseConfig {
    @NotBlank(message = "The database host is required")
    private String host;
    @Min(value = 1024, message = "The port must be greater than or equal to 1024")
    @Max(value = 65535, message = "The port must be less than or equal to 65535")
    private int port;
    @NotBlank(message = "The database user is required")
    private String user;
    @NotBlank(message = "The database password is required")
    private String password;
    @NotBlank(message = "The database name is required")
    private String databaseName;
    @NotBlank(message = "The database type is required - Should be either POSTGRES or MYSQL")
    private DatabaseType databaseType;
    @Null(message = "The schema is not required for MySQL")
    private String schema;
}
