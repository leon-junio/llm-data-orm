package com.leonjr.ldo.validation.models;

import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LocalValidationResult {
    private double fieldCoverage;
    private double entropyScore;
    private double precisionRecall;
    private double jaccardSimilarity;
    private double f1Score;
    private Set<String> missingMandatoryFields;
    private Map<String, String> dataTypeErrors;
}