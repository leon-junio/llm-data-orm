package com.leonjr.ldo.validation.models;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Data;

@Data
public class LocalSimpleValidationResult {
    private List<String> missingMandatoryFields;
    private List<String> dataTypeErrors;
    private Pair<Double, Double> conformityAndUnknownRate;

    @Override
    public String toString() {
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
}