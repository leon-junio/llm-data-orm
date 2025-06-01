package com.leonjr.ldo.database.models;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.leonjr.ldo.app.helper.JsonHelper;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TableDescription {
    @NotBlank(message = "Table name cannot be blank")
    private String name;
    private List<ColumnDescription> columns;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Table: ").append(name).append("\n");
        if (columns != null) {
            columns.forEach(column -> sb.append(column.toString()).append("\n"));
        }
        return sb.toString();
    }

    public JsonNode getFullJsonSchemaFromToJson() throws Exception{
        String json = toJson();
        return JsonHelper.convertJsonStringToJsonNode(json);
    }

    /**
     * Validates whether a JSON node's data type is compatible with a specified database column type.
     * 
     * This method performs type checking by examining the JSON node's data type and comparing it
     * against a list of compatible database column types. The validation supports common database
     * types including integers, floating-point numbers, booleans, and text-based types.
     * 
     * @param json The JsonNode to validate. If null or represents a JSON null value, validation fails.
     * @param columnType The database column type to check compatibility against. Case-insensitive
     *                   as it will be converted to uppercase internally.
     * @return true if the JSON node's type is compatible with the specified column type, false otherwise.
     *         Returns false for null or JSON null values.
     * 
     * Supported type mappings:
     * - Integer/Long JSON values: Compatible with INT, BIGINT column types
     * - Floating-point JSON values: Compatible with FLOAT, DOUBLE, DECIMAL column types  
     * - Boolean JSON values: Compatible with BOOLEAN, BIT column types
     * - String JSON values: Compatible with CHAR, VARCHAR, TEXT, DATE, TIMESTAMP, DATETIME, TIME column types
     */
    public static boolean checkIfJsonTypeIsValid(JsonNode json, String columnType) {
        if (json == null || json.isNull())
            return false;

        columnType = columnType.toUpperCase();

        if (json.isInt() || json.isLong()) {
            return List.of("INT", "BIGINT").contains(columnType);
        } else if (json.isFloatingPointNumber()) {
            return List.of("FLOAT", "DOUBLE", "DECIMAL").contains(columnType);
        } else if (json.isBoolean()) {
            return List.of("BOOLEAN", "BIT").contains(columnType);
        } else if (json.isTextual()) {
            return List.of("CHAR", "VARCHAR", "TEXT", "DATE", "TIMESTAMP", "DATETIME", "TIME").contains(columnType);
        }

        return false;
    }

    /**
     * Convert the object to JSON string
     * 
     * @return JSON string representation of the object
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
