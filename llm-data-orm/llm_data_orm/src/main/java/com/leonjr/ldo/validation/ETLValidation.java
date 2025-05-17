package com.leonjr.ldo.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.database.models.TableDescription;
import com.leonjr.ldo.validation.helper.LocalHelper;
import com.leonjr.ldo.validation.models.LocalSimpleValidationResult;
import com.leonjr.ldo.validation.models.LocalValidationResult;

public class ETLValidation {

    /**
     * Validate the parsed JSON against the target JSON and the schema. This is
     * useful for checking the parsed JSON against the target testing JSON and the
     * schema.
     * 
     * @param targetJson       The target JSON to be compared with.
     * @param parsedJson       The parsed JSON to be validated.
     * @param tableDescription The table description containing the schema.
     * @return A LocalValidationResult object containing the validation results.
     */
    public static LocalValidationResult validateParsingWithTestJson(JsonNode targetJson, JsonNode parsedJson,
            TableDescription tableDescription) throws Exception {
        LocalValidationResult result = new LocalValidationResult();
        var missingMandatoryFields = LocalHelper.checkMandatoryFields(parsedJson,
                tableDescription.getFullJsonSchemaFromToJson().get("columns"));
        var dataTypeErrors = LocalHelper.checkDataTypes(parsedJson,
                tableDescription.getFullJsonSchemaFromToJson().get("columns"));
        var conformityAndUnknownRate = LocalHelper.conformityAndUnknownRate(parsedJson,
                tableDescription.getFullJsonSchemaFromToJson().get("columns"));
        var precisionRecallF1 = LocalHelper.precisionRecallF1(targetJson, parsedJson);
        var jaccardSimilarity = LocalHelper.jaccardSimilarity(targetJson, parsedJson);
        result.setMissingMandatoryFields(missingMandatoryFields);
        result.setDataTypeErrors(dataTypeErrors);
        result.setConformityAndUnknownRate(conformityAndUnknownRate);
        result.setPrecisionRecallF1(precisionRecallF1);
        result.setJaccardSimilarity(jaccardSimilarity);
        return result;
    }

    /**
     * * Validate the parsed JSON without comparing it to the target JSON. This is
     * useful for checking the parsed JSON against the schema and when does not have
     * a target testing JSON.
     * 
     * @param parsedJson       The parsed JSON to be validated.
     * @param tableDescription The table description containing the schema.
     * @return A LocalValidationResult object containing the validation results.
     */
    public static LocalSimpleValidationResult validateParsingLocally(JsonNode parsedJson,
            TableDescription tableDescription) throws Exception {
        LocalSimpleValidationResult result = new LocalSimpleValidationResult();
        var missingMandatoryFields = LocalHelper.checkMandatoryFields(parsedJson,
                tableDescription.getFullJsonSchemaFromToJson().get("columns"));
        var dataTypeErrors = LocalHelper.checkDataTypes(parsedJson,
                tableDescription.getFullJsonSchemaFromToJson().get("columns"));
        var conformityAndUnknownRate = LocalHelper.conformityAndUnknownRate(parsedJson,
                tableDescription.getFullJsonSchemaFromToJson().get("columns"));
        result.setMissingMandatoryFields(missingMandatoryFields);
        result.setDataTypeErrors(dataTypeErrors);
        result.setConformityAndUnknownRate(conformityAndUnknownRate);
        return result;
    }
}
