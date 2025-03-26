package com.leonjr.ldo.extractor.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonResponseTransformer {
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
