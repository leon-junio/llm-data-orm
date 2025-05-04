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

    public JsonNode getFullJsonSchemaFromToJson() {
        String json = toJson();
        return JsonHelper.convertJsonStringToJsonNode(json);
    }

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
