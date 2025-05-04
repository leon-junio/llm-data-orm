package com.leonjr.ldo.database.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColumnDescription {

    @NotBlank(message = "Column name cannot be blank")
    private String name;
    @NotBlank(message = "Column type cannot be blank")
    private String type;
    @NotEmpty(message = "Column size cannot be empty")
    private int size;
    private boolean nullable;
    @NotBlank(message = "Auto increment cannot be blank")
    private String autoIncrement;
    private String defaultValue;

    public String toString() {
        return String.format(
                "Name: %s | Type: %s(%d) | Nullable: %s | Auto Increment: %s | Default: %s | Primary Key: %s",
                name, type, size, (nullable ? "YES" : "NO"), autoIncrement, defaultValue,
                isPrimaryKey() ? "YES" : "NO");
    }

    public boolean isPrimaryKey() {
        return "YES".equalsIgnoreCase(autoIncrement);
    }

}
