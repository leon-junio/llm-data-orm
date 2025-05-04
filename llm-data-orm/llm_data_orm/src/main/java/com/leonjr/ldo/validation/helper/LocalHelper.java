package com.leonjr.ldo.validation.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.database.models.TableDescription;

public final class LocalHelper {

    public static Set<String> flattenJson(JsonNode node, String prefix) {
        Set<String> result = new HashSet<>();
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(field -> {
                result.addAll(flattenJson(node.get(field), prefix + "." + field));
            });
        } else if (node.isArray()) {
            int index = 0;
            for (JsonNode item : node) {
                result.addAll(flattenJson(item, prefix + "[" + index + "]"));
                index++;
            }
        } else {
            result.add(prefix + "=" + node.asText());
        }
        return result;
    }

    public static double jaccardSimilarity(JsonNode testJson, JsonNode actualJson) {
        Set<String> testFlatten = flattenJson(testJson, "");
        Set<String> actualFlatten = flattenJson(actualJson, "");

        Set<String> intersection = new HashSet<>(testFlatten);
        intersection.retainAll(actualFlatten);
        Set<String> union = new HashSet<>(testFlatten);
        union.addAll(actualFlatten);
        return (double) intersection.size() / union.size();
    }

    public static Map<String, Integer> precisionRecallF1(JsonNode testJson, JsonNode actualJson) {
        Set<String> expected = flattenJson(testJson, "");
        Set<String> actual = flattenJson(actualJson, "");

        Set<String> tp = new HashSet<>(actual);
        tp.retainAll(expected);

        Set<String> fp = new HashSet<>(actual);
        fp.removeAll(expected);

        Set<String> fn = new HashSet<>(expected);
        fn.removeAll(actual);

        int truePos = tp.size();
        int falsePos = fp.size();
        int falseNeg = fn.size();

        return Map.of(
                "TP", truePos,
                "FP", falsePos,
                "FN", falseNeg);
    }

    public static List<String> checkDataTypes(JsonNode dataJson, JsonNode tableStructure) {
        List<String> mismatches = new ArrayList<>();
        for (JsonNode field : tableStructure) {
            String name = field.get("name").asText();
            String expectedType = field.get("type").asText();
            JsonNode value = dataJson.get(name);
            if (value != null) {
                boolean typeMismatch = switch (expectedType) {
                    case "INT" -> !value.isInt();
                    case "STRING" -> !value.isTextual();
                    case "BOOLEAN" -> !value.isBoolean();
                    case "FLOAT" -> !value.isFloatingPointNumber();
                    default -> false;
                };
                if (typeMismatch) {
                    mismatches.add(name + " has type " + value.getNodeType() + ", expected " + expectedType);
                }
            }
        }
        return mismatches;
    }

    public static List<String> checkMandatoryFields(JsonNode dataJsonArray, JsonNode tableStructure) {
        List<String> missingFields = new ArrayList<>();

        if (!dataJsonArray.isArray()) {
            throw new IllegalArgumentException("Expected an array of data rows.");
        }

        int index = 0;
        for (JsonNode dataItem : dataJsonArray) {
            for (JsonNode field : tableStructure) {
                String fieldName = field.get("name").asText();
                boolean nullable = field.get("nullable").asBoolean();
                String autoIncrement = field.has("autoIncrement") ? field.get("autoIncrement").asText() : "NO";
                boolean hasDefault = field.has("defaultValue") && !field.get("defaultValue").isNull();

                // Pular campos com autoincremento ou valor default
                if ("YES".equalsIgnoreCase(autoIncrement) || hasDefault) {
                    continue;
                }

                // Checa se está ausente OU é nulo (duas condições diferentes!)
                if (!nullable && (!dataItem.has(fieldName) || dataItem.get(fieldName).isNull())) {
                    missingFields.add("Row " + index + ": missing mandatory field '" + fieldName + "'");
                }
            }
            index++;
        }

        return missingFields;
    }

    public static Pair<Double, Double> conformityAndUnknownRate(JsonNode dataJsonArray, JsonNode tableStructure) {
        if (!dataJsonArray.isArray()) {
            throw new IllegalArgumentException("Expected dataJson to be an array.");
        }

        // Mapeia nome do campo → tipo esperado
        Map<String, String> tableFieldTypes = new HashMap<>();
        for (JsonNode column : tableStructure) {
            String name = column.get("name").asText();
            String type = column.get("type").asText();

            String autoIncrement = column.has("autoIncrement") ? column.get("autoIncrement").asText() : "NO";
            boolean hasDefault = column.has("defaultValue") && !column.get("defaultValue").isNull();

            if ("YES".equalsIgnoreCase(autoIncrement) || hasDefault) {
                continue; // Ignora campos que não são esperados no JSON
            }

            tableFieldTypes.put(name, type);
        }

        int totalChecked = 0;
        int validFields = 0;
        int unknownFields = 0;

        for (JsonNode dataItem : dataJsonArray) {
            Iterator<Map.Entry<String, JsonNode>> fields = dataItem.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode value = entry.getValue();

                totalChecked++;

                if (tableFieldTypes.containsKey(fieldName)) {
                    String expectedType = tableFieldTypes.get(fieldName);
                    if (TableDescription.checkIfJsonTypeIsValid(value, expectedType)) {
                        validFields++;
                    } else {
                        unknownFields++;
                    }
                } else {
                    // Campo não está na tabela
                    unknownFields++;
                }
            }
        }

        double conformity = totalChecked == 0 ? 0.0 : (double) validFields / totalChecked;
        double unknownRate = totalChecked == 0 ? 0.0 : (double) unknownFields / totalChecked;

        return Pair.of(conformity, unknownRate);
    }
}
