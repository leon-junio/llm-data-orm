package com.leonjr.ldo.database.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public JsonNode getJsonSchema() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schema = mapper.createObjectNode();
        ObjectNode properties = mapper.createObjectNode();

        if (columns != null) {
            for (ColumnDescription column : columns) {
                ObjectNode fieldSchema = mapper.createObjectNode();
                fieldSchema.put("type", mapColumnTypeToJsonType(column.getType())); // Método para mapear tipos
                properties.set(column.getName(), fieldSchema);
            }
        }

        schema.put("type", "object");
        schema.set("properties", properties);
        return schema;
    }

    private String mapColumnTypeToJsonType(String columnType) {
        Map<String, String> typeMapping = new HashMap<>();
        typeMapping.put("INT", "integer");
        typeMapping.put("BIGINT", "integer");
        typeMapping.put("DECIMAL", "number");
        typeMapping.put("FLOAT", "number");
        typeMapping.put("DOUBLE", "number");
        typeMapping.put("BOOLEAN", "boolean");
        typeMapping.put("CHAR", "string");
        typeMapping.put("BIT", "boolean");
        typeMapping.put("VARCHAR", "string");
        typeMapping.put("TEXT", "string");
        typeMapping.put("DATE", "string");
        typeMapping.put("TIMESTAMP", "string");

        return typeMapping.getOrDefault(columnType.toUpperCase(), "string");
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
