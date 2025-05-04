package com.leonjr.ldo.validation.models;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Data;

@Data
public class LocalValidationResult {
    private Map<String, Integer> precisionRecallF1;
    private double jaccardSimilarity;
    private List<String> missingMandatoryFields;
    private List<String> dataTypeErrors;
    private Pair<Double, Double> conformityAndUnknownRate;

    private double precision(int tp, int fp) {
        return tp + fp == 0 ? 0.0 : tp / (double) (tp + fp);
    }

    private double recall(int tp, int fn) {
        return tp + fn == 0 ? 0.0 : tp / (double) (tp + fn);
    }

    private double f1Score(double precision, double recall) {
        return (precision + recall) == 0.0 ? 0.0 : 2 * (precision * recall) / (precision + recall);
    }

    public String simpleValidationResult() {
        return String.format("{" +
                "Missing Mandatory Fields: %s%n" +
                "Data Type Errors: %s%n" +
                "Conformity Rate: %.4f%n" +
                "Unknown Rate: %.4f%n" +
                "}", missingMandatoryFields,
                dataTypeErrors,
                conformityAndUnknownRate.getLeft(),
                conformityAndUnknownRate.getRight());
    }

    @Override
    public String toString() {
        return String.format("{" +
                "Precision: %.4f%n" +
                "Recall: %.4f%n" +
                "F1 Score: %.4f%n" +
                "Jaccard Similarity: %.4f%n" +
                "Missing Mandatory Fields: %s%n" +
                "Data Type Errors: %s%n" +
                "Conformity Rate: %.4f%n" +
                "Unknown Rate: %.4f%n" +
                "}",
                precision(precisionRecallF1.get("TP"), precisionRecallF1.get("FP")),
                recall(precisionRecallF1.get("TP"), precisionRecallF1.get("FN")),
                f1Score(precision(precisionRecallF1.get("TP"), precisionRecallF1.get("FP")),
                        recall(precisionRecallF1.get("TP"), precisionRecallF1.get("FN"))),
                jaccardSimilarity,
                missingMandatoryFields,
                dataTypeErrors,
                conformityAndUnknownRate.getLeft(),
                conformityAndUnknownRate.getRight());

    }
}