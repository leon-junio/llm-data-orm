package com.leonjr.ldo.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.database.models.TableDescription;
import com.leonjr.ldo.validation.helper.LocalHelper;
import com.leonjr.ldo.validation.models.LocalValidationResult;

public class ETLValidation {
    public static LocalValidationResult validateParsingLocally(String originalText, JsonNode parsedJson,
            TableDescription tableDescription, String parsedJsonString) {
        LocalValidationResult result = new LocalValidationResult();
        double entropy = LocalHelper.calculateEntropy(originalText, parsedJson);
        result.setEntropyScore(entropy);
        var mandatoryFieldsCheck = LocalHelper.checkMandatoryFields(parsedJson, tableDescription);
        result.setMissingMandatoryFields(mandatoryFieldsCheck);
        var dataTypeErrors = LocalHelper.checkDataTypes(parsedJson, tableDescription);
        result.setDataTypeErrors(dataTypeErrors);
        result.setFieldCoverage(
                (double) (tableDescription.getColumns().size() - mandatoryFieldsCheck.size() - dataTypeErrors.size())
                        / tableDescription.getColumns().size());
        result.setPrecisionRecall(LocalHelper.calculatePrecisionRecall(originalText, parsedJson));
        result.setJaccardSimilarity(LocalHelper.calculateJaccardSimilarity(originalText, parsedJsonString));
        result.setF1Score(LocalHelper.calculateF1Score(result.getPrecisionRecall(), result.getJaccardSimilarity()));
        return result;
    }
}
