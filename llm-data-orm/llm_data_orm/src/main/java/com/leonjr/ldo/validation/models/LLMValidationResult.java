package com.leonjr.ldo.validation.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents the result of a validation performed by a Large Language
 * Model (LLM).
 *
 * <p>
 * This class stores various qualitative metrics assessed by an LLM, including:
 * <ul>
 * <li>Coherence</li>
 * <li>Completeness</li>
 * <li>Fidelity</li>
 * <li>Freedom from Hallucination</li>
 * <li>Self-Awareness (Recognition of Limitations)</li>
 * <li>Honesty (Ability to Reject Out-of-Scope Requests)</li>
 * </ul>
 * It also includes an aggregated reliability score, an acceptance status based
 * on this score,
 * and a textual justification provided by the LLM.
 * </p>
 *
 * @see PromptTexts#_VALIDATION_TEXT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LLMValidationResult {

    @JsonProperty("coherenceScore")
    private double coherenceScore;

    @JsonProperty("completenessScore")
    private double completenessScore;

    @JsonProperty("fidelityScore")
    private double fidelityScore;

    @JsonProperty("freedomFromHallucinationScore")
    private double freedomFromHallucinationScore;

    @JsonProperty("selfAwarenessScore")
    private double selfAwarenessScore;

    @JsonProperty("honestyScore")
    private double honestyScore;

    @JsonProperty("aggregatedReliabilityScore")
    private double aggregatedReliabilityScore;

    @JsonProperty("acceptanceStatus")
    private String acceptanceStatus; // WILL BE "ACCEPTED" or "REJECTED"

    @JsonProperty("justification")
    private String justification;

    /**
     * Provides a simple string representation of the validation result, focusing on
     * key metrics.
     *
     * @return A formatted string summarizing the validation.
     */
    public String simpleValidationResult() {
        return String.format(
                "{\n" +
                        "  Aggregated Reliability: %.2f%%%n" +
                        "  Acceptance Status: %s%n" +
                        "  Justification: %s%n" +
                        "}",
                aggregatedReliabilityScore,
                acceptanceStatus,
                justification);
    }

    /**
     * Provides a comprehensive string representation of all validation metrics.
     *
     * @return A formatted string detailing all validation scores.
     */
    @Override
    public String toString() {
        return String.format(
                "{\n" +
                        "  Coherence Score: %.2f%n" +
                        "  Completeness Score: %.2f%n" +
                        "  Fidelity Score: %.2f%n" +
                        "  Freedom from Hallucination Score: %.2f%n" +
                        "  Self-Awareness Score: %.2f%n" +
                        "  Honesty Score: %.2f%n" +
                        "  Aggregated Reliability Score: %.2f%%%n" +
                        "  Acceptance Status: %s%n" +
                        "  Justification: %s%n" +
                        "}",
                coherenceScore,
                completenessScore,
                fidelityScore,
                freedomFromHallucinationScore,
                selfAwarenessScore,
                honestyScore,
                aggregatedReliabilityScore,
                acceptanceStatus,
                justification);
    }

    /**
     * Converts this object into a JSON string.
     *
     * @return JSON string representation of this object.
     * @throws JsonProcessingException if an error occurs during JSON serialization.
     */
    public String toJsonString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    /**
     * Creates an LLMValidationResult instance from a JSON string.
     *
     * @param jsonString The JSON string to parse.
     * @return An instance of LLMValidationResult.
     * @throws JsonProcessingException if an error occurs during JSON
     *                                 deserialization.
     */
    public static LLMValidationResult fromJsonString(String jsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, LLMValidationResult.class);
    }

    /**
     * Determines whether this validation result meets the acceptance criteria.
     * 
     * A validation result is considered accepted if both conditions are met:
     * - The aggregated reliability score is greater than or equal to the specified
     * threshold
     * - The acceptance status is "ACCEPTED" (case-insensitive)
     * 
     * @param threshold the minimum reliability score required for acceptance
     * @return true if the validation result is accepted based on the threshold and
     *         status, false otherwise
     */
    public boolean isAccepted(double threshold) {
        return aggregatedReliabilityScore >= threshold
                && acceptanceStatus.equalsIgnoreCase("ACCEPTED");
    }
}