package com.leonjr.ldo.validation.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;

import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.database.models.TableDescription;

public final class LocalHelper {

    public static double jaccardSimilarity(JsonNode testJson, JsonNode actualJson) {
        Set<String> testFlatten = flattenJsonNormalized(testJson, "");
        Set<String> actualFlatten = flattenJsonNormalized(actualJson, "");

        Set<String> intersection = new HashSet<>(testFlatten);
        intersection.retainAll(actualFlatten);
        Set<String> union = new HashSet<>(testFlatten);
        union.addAll(actualFlatten);
        return (double) intersection.size() / union.size();
    }

    private static final int STRING_SIMILARITY_THRESHOLD = 80;

    public static Map<String, Integer> precisionRecallF1(JsonNode testJson, JsonNode actualJson) {
        Set<String> expected = flattenJsonNormalized(testJson, "");
        Set<String> actual = flattenJsonNormalized(actualJson, "");

        Set<String> truePositives = new HashSet<>();
        Set<String> falsePositives = new HashSet<>(actual);
        Set<String> falseNegatives = new HashSet<>(expected);

        // Avaliar matches com tolerância textual para strings
        for (String actualEntry : actual) {
            Optional<String> match = expected.stream()
                    .filter(expectedEntry -> isSimilar(actualEntry, expectedEntry))
                    .findFirst();

            if (match.isPresent()) {
                truePositives.add(actualEntry);
                falsePositives.remove(actualEntry);
                falseNegatives.remove(match.get());
            }
        }

        int tp = truePositives.size();
        int fp = falsePositives.size();
        int fn = falseNegatives.size();

        return Map.of(
                "TP", tp,
                "FP", fp,
                "FN", fn);
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
                continue;
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

    private static boolean isSimilar(String a, String b) {
        if (a.equals(b))
            return true;

        if (a.matches(".*\\d.*") || b.matches(".*\\d.*")) {
            return false;
        }

        LevenshteinDistance ld = LevenshteinDistance.getDefaultInstance();
        int distance = ld.apply(a, b);
        int maxLen = Math.max(a.length(), b.length());
        int similarityPercent = maxLen == 0 ? 100 : (100 * (maxLen - distance)) / maxLen;

        return similarityPercent >= STRING_SIMILARITY_THRESHOLD;
    }

    private static Set<String> flattenJsonNormalized(JsonNode node, String prefix) {
        Set<String> result = new HashSet<>();

        if (node.isArray()) {
            int index = 0;
            for (JsonNode element : node) {
                if (element.isNull()) {
                    continue;
                }
                result.addAll(flattenJsonNormalized(element, prefix + "[" + index + "]."));
                index++;
            }
        } else if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (entry.getValue().isNull()) {
                    continue;
                }
                String key = prefix + entry.getKey();
                result.addAll(flattenJsonNormalized(entry.getValue(), key + "."));
            }
        } else {
            String value = node.isNull() ? "null" : node.asText();
            result.add(prefix.substring(0, prefix.length() - 1) + "=" + value);
        }

        return result;
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

                if ("YES".equalsIgnoreCase(autoIncrement) || hasDefault) {
                    continue;
                }

                if (!nullable && (!dataItem.has(fieldName) || dataItem.get(fieldName).isNull())) {
                    missingFields.add("Row " + index + ": missing mandatory field '" + fieldName + "'");
                }
            }
            index++;
        }

        return missingFields;
    }
}
