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

    private static final int STRING_SIMILARITY_THRESHOLD = 80;

    /**
     * Calculates the Jaccard similarity coefficient between two JSON objects.
     * 
     * The Jaccard similarity is computed by flattening both JSON objects into normalized
     * sets of key-value pairs, then calculating the ratio of the intersection size to
     * the union size of these sets.
     * 
     * @param testJson the first JSON object to compare
     * @param actualJson the second JSON object to compare
     * @return a double value between 0.0 and 1.0 representing the similarity,
     *         where 1.0 indicates identical JSON structures and 0.0 indicates
     *         completely different structures
     */
    public static double jaccardSimilarity(JsonNode testJson, JsonNode actualJson) {
        Set<String> testFlatten = flattenJsonNormalized(testJson, "");
        Set<String> actualFlatten = flattenJsonNormalized(actualJson, "");

        Set<String> intersection = new HashSet<>(testFlatten);
        intersection.retainAll(actualFlatten);
        Set<String> union = new HashSet<>(testFlatten);
        union.addAll(actualFlatten);
        return (double) intersection.size() / union.size();
    }

    /**
     * Calculates precision, recall, and F1 score metrics by comparing two JSON
     * structures.
     * 
     * This method flattens both JSON objects into normalized string sets and
     * performs
     * similarity-based matching with text tolerance to determine true positives,
     * false positives, and false negatives.
     * 
     * @param testJson   the expected/ground truth JSON structure to compare against
     * @param actualJson the actual/predicted JSON structure being evaluated
     * @return a Map containing the counts of:
     *         - "TP" (True Positives): correctly identified matches
     *         - "FP" (False Positives): incorrectly identified as matches
     *         - "FN" (False Negatives): missed expected matches
     * 
     * @throws NullPointerException if either testJson or actualJson is null
     * 
     * @see #flattenJsonNormalized(JsonNode, String) for JSON flattening logic
     * @see #isSimilar(String, String) for similarity matching criteria
     */
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

    /**
     * Calculates the conformity rate and unknown field rate for JSON data against a
     * table structure.
     * 
     * This method validates JSON data against a defined table structure by checking
     * field types
     * and presence. Fields with auto-increment or default values are excluded from
     * validation.
     * 
     * @param dataJsonArray  the JSON array containing data records to validate
     * @param tableStructure the JSON array defining the table structure with
     *                       columns containing
     *                       "name", "type", and optionally "autoIncrement" and
     *                       "defaultValue" fields
     * @return a Pair containing:
     *         - First element: conformity rate (0.0 to 1.0) representing the ratio
     *         of valid fields to total fields
     *         - Second element: unknown rate (0.0 to 1.0) representing the ratio of
     *         unknown/invalid fields to total fields
     * @throws IllegalArgumentException if dataJsonArray is not a JSON array
     */
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

    /**
     * Determines if two strings are similar based on their Levenshtein distance.
     * 
     * <p>
     * This method first checks for exact equality. If the strings are not equal,
     * it verifies that neither string contains digits (as strings with digits are
     * considered dissimilar). Finally, it calculates the similarity percentage
     * using Levenshtein distance and compares it against a predefined threshold.
     * </p>
     * 
     * @param a the first string to compare
     * @param b the second string to compare
     * @return {@code true} if the strings are exactly equal, or if both strings
     *         contain no digits and their similarity percentage meets or exceeds
     *         the {@code STRING_SIMILARITY_THRESHOLD}; {@code false} otherwise
     */
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

    /**
     * Recursively flattens a JsonNode into a set of normalized key-value string
     * representations.
     * 
     * This method traverses a JSON structure and converts it into a flat set of
     * strings where each
     * string represents a path-value pair. Arrays are indexed with brackets
     * notation, and objects
     * use dot notation for property access.
     * 
     * @param node   the JsonNode to flatten (can be object, array, or primitive
     *               value)
     * @param prefix the current path prefix used for building the complete property
     *               path
     * @return a Set of strings where each string follows the format "path=value"
     *         (e.g., "user.address[0].street=Main St", "user.age=25")
     * 
     * @implNote Null values are skipped during traversal. For leaf nodes, the
     *           trailing dot
     *           from the prefix is removed before creating the final key-value
     *           pair.
     * 
     * @see JsonNode
     */
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

    /**
     * Validates data types in a JSON object against expected table structure
     * definitions.
     * 
     * This method compares the actual data types of fields in the provided JSON
     * data
     * with the expected types defined in the table structure schema. It identifies
     * and reports any type mismatches found during validation.
     * 
     * @param dataJson       The JSON node containing the actual data to be
     *                       validated.
     *                       Each field in this JSON will be checked against the
     *                       corresponding
     *                       field definition in the table structure.
     * @param tableStructure The JSON node containing the table structure
     *                       definition.
     *                       Each element should have "name" and "type" fields that
     *                       define the expected field name and data type.
     *                       Supported types: "INT", "STRING", "BOOLEAN", "FLOAT".
     * @return A list of strings describing type mismatches. Each string contains
     *         the field name, actual type found, and expected type. Returns an
     *         empty list if no mismatches are found or if fields are missing from
     *         dataJson.
     * 
     * @throws NullPointerException     if dataJson or tableStructure is null
     * @throws IllegalArgumentException if table structure fields are missing
     *                                  required "name" or "type" properties
     */
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

    /**
     * Validates that all mandatory fields are present and non-null in the provided
     * data rows.
     * 
     * This method checks each data item against the table structure to ensure that
     * all
     * non-nullable fields (that don't have auto-increment or default values) are
     * present
     * and contain non-null values.
     * 
     * @param dataJsonArray  A JsonNode containing an array of data rows to
     *                       validate.
     *                       Each row should be a JSON object with field names as
     *                       keys.
     * @param tableStructure A JsonNode containing an array of field definitions.
     *                       Each field definition should have:
     *                       - "name": the field name (required)
     *                       - "nullable": boolean indicating if the field can be
     *                       null (required)
     *                       - "autoIncrement": string ("YES"/"NO") indicating
     *                       auto-increment status (optional)
     *                       - "defaultValue": the default value for the field
     *                       (optional)
     * 
     * @return A list of error messages describing missing mandatory fields.
     *         Each message includes the row index and field name.
     *         Returns an empty list if all mandatory fields are present.
     * 
     * @throws IllegalArgumentException if the dataJsonArray parameter is not an
     *                                  array
     */
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
