package com.leonjr.ldo.app.helper;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelper {

    /**
     * Convert a JSON string to a JsonNode object.
     * 
     * @param jsonString the JSON string to convert
     * @return the JsonNode object
     */
    public static JsonNode convertJsonStringToJsonNode(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON string to JsonNode", e);
        }
    }

    /**
     * Read a JSON file and convert it to a JsonNode object.
     * 
     * @param filePath the path to the JSON file
     * @return the JsonNode object
     */
    public static JsonNode readFileAsJsonNode(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var file = new File(filePath);
            if (!file.exists()) {
                throw new IllegalArgumentException("File does not exist: " + filePath);
            }
            return objectMapper.readTree(file);
        } catch (Exception e) {
            throw new RuntimeException("Error reading file as JsonNode", e);
        }
    }

}
