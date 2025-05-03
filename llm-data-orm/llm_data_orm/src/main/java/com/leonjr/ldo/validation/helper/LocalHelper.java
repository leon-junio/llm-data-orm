package com.leonjr.ldo.validation.helper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import opennlp.tools.tokenize.SimpleTokenizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.database.models.TableDescription;

public final class LocalHelper {
    public static double calculateEntropy(String text, JsonNode json) {
        Map<String, Integer> textTermFrequency = calculateTermFrequency(text);
        Map<String, Integer> jsonTermFrequency = calculateTermFrequency(json.toString());

        double entropy = 0.0;
        double epsilon = 1e-10; // Para evitar log(0)

        int textLength = text.length();
        int jsonLength = json.toString().length();

        for (String term : textTermFrequency.keySet()) {
            double p = textTermFrequency.get(term) / (double) textLength;
            double q = jsonTermFrequency.getOrDefault(term, 0) / (double) jsonLength + epsilon; // Evita 0

            entropy += p * Math.log(p / q);
        }

        return -entropy; // A entropia é sempre positiva
    }

    public static Set<String> checkMandatoryFields(JsonNode json, TableDescription tableDescription) {
        Set<String> missingFields = new HashSet<>();
        JsonNode schema = tableDescription.getJsonSchema();
        JsonNode requiredFields = schema.get("required");
        if (requiredFields != null) {
            requiredFields.forEach(field -> {
                if (json.get(field.asText()) == null) {
                    missingFields.add(field.asText());
                }
            });
        }
        return missingFields;
    }

    public static Map<String, String> checkDataTypes(JsonNode json, TableDescription tableDescription) {
        Map<String, String> errors = new HashMap<>();
        JsonNode schema = tableDescription.getJsonSchema();

        JsonNode properties = schema.get("properties");
        properties.fieldNames().forEachRemaining(field -> {
            String expectedType = properties.get(field).get("type").asText();
            JsonNode value = json.get(field);
            if (value != null && !value.isNull()) {
                String actualType = value.getNodeType().name().toLowerCase();
                if (!actualType.equals(expectedType)) {
                    errors.put(field, "Expected: " + expectedType + ", Actual: " + actualType);
                }
            }
        });
        return errors;
    }

    public static Map<String, Double> simpleCalculateTermFrequency(String text) {
        return Arrays.stream(text.split("\\s+"))
                .filter(word -> word.length() > 3)
                .collect(Collectors.groupingBy(
                        String::toLowerCase,
                        Collectors.summingDouble(w -> 1.0)));
    }

    public static Map<String, Integer> calculateTermFrequency(String text) {
        Map<String, Integer> termFrequency = new HashMap<>();
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize(text.toLowerCase());

        for (String token : tokens) {
            termFrequency.put(token, termFrequency.getOrDefault(token, 0) + 1);
        }

        return termFrequency;
    }

    public static double calculateJaccardSimilarity(String text1, String text2) {

        Set<String> set1 = Arrays.stream(text1.toLowerCase().split("\\s+"))
                .collect(Collectors.toSet());
        Set<String> set2 = Arrays.stream(text2.toLowerCase().split("\\s+"))
                .collect(Collectors.toSet());

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    public static double calculatePrecisionRecall(String original, JsonNode json) {
        Set<String> originalTerms = calculateTermFrequency(original).keySet();
        Set<String> jsonTerms = calculateTermFrequency(json.toString()).keySet();

        long truePositives = jsonTerms.stream()
                .filter(originalTerms::contains)
                .count();

        double precision = (double) truePositives / jsonTerms.size();
        double recall = (double) truePositives / originalTerms.size();

        return 2 * (precision * recall) / (precision + recall);
    }

    public static double calculateF1Score(double precisionRecall, double jaccardSimilarity) {
        return 2 * (precisionRecall * jaccardSimilarity) / (precisionRecall + jaccardSimilarity);
    }
}
