package com.leonjr.ldo.extractor.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonResponseTransformer {
    /**
     * Parses a JSON string containing nested arrays and flattens them into a single
     * merged array.
     * 
     * This method takes a JSON response string that represents an array of arrays
     * and flattens
     * all nested arrays into a single JsonArray. Only elements that are JsonArrays
     * themselves
     * are processed and their contents are added to the merged result.
     * 
     * @param jsonResponse the JSON string to parse, expected to be an array
     *                     containing nested arrays
     * @return a JSON string representation of the flattened array containing all
     *         elements
     *         from the nested arrays in the original input
     * @throws JsonSyntaxException   if the input string is not valid JSON
     * @throws IllegalStateException if the root element is not a JsonArray
     */
    public static String parseJson(String jsonResponse) {
        JsonArray rootArray = JsonParser.parseString(jsonResponse).getAsJsonArray();
        JsonArray mergedArray = new JsonArray();
        for (JsonElement element : rootArray) {
            if (element.isJsonArray()) {
                for (JsonElement obj : element.getAsJsonArray()) {
                    mergedArray.add(obj);
                }
            }
        }
        String finalJson = new Gson().toJson(mergedArray);
        return finalJson;
    }

    /**
     * Parses a JSON string into a JsonArray and flattens any nested arrays into a
     * single merged array.
     * 
     * This method takes a JSON string representation of an array and processes it
     * by:
     * 1. Parsing the JSON string into a JsonArray
     * 2. Iterating through each element in the root array
     * 3. If an element is itself a JsonArray, extracting all its elements and
     * adding them to the merged result
     * 4. Non-array elements are ignored and not included in the final result
     * 
     * @param jsonResponse the JSON string to be parsed, expected to contain an
     *                     array structure
     * @return a flattened JsonArray containing all elements from nested arrays
     *         within the input
     * @throws JsonSyntaxException   if the jsonResponse is not valid JSON
     * @throws IllegalStateException if the root element is not a JsonArray
     */
    public static JsonArray parseJsonArray(String jsonResponse) {
        JsonArray rootArray = JsonParser.parseString(jsonResponse).getAsJsonArray();
        JsonArray mergedArray = new JsonArray();
        for (JsonElement element : rootArray) {
            if (element.isJsonArray()) {
                for (JsonElement obj : element.getAsJsonArray()) {
                    mergedArray.add(obj);
                }
            }
        }
        return mergedArray;
    }
}
