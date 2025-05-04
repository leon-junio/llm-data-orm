package com.leonjr.ldo.database.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

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

    private JsonNode convertJsonStringToJsonNode(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON string to JsonNode", e);
        }
    }

    public JsonNode getFullJsonSchemaFromToJson() {
        String json = toJson();
        return convertJsonStringToJsonNode(json);
    }

    public static boolean checkIfJsonTypeIsValid(JsonNode json, String columnType) {
        var jsonType = json.getNodeType().name().toLowerCase();
        Map<String, List<String>> typeMapping = new HashMap<>();
        typeMapping.put("integer", List.of("INT", "BIGINT"));
        typeMapping.put("number", List.of("DECIMAL", "FLOAT", "DOUBLE"));
        typeMapping.put("boolean", List.of("BOOLEAN", "BIT"));
        typeMapping.put("string", List.of("CHAR", "VARCHAR", "TEXT", "DATE", "TIMESTAMP", "DATETIME",
                "TIME"));

        return typeMapping.getOrDefault(jsonType, List.of()).contains(columnType.toUpperCase());
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
