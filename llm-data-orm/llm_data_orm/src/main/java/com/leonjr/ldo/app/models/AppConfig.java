package com.leonjr.ldo.app.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AppConfig {
    @NotBlank(message = "Target table name is required")
    private String targetTableName;
}
