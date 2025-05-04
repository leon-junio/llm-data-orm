package com.leonjr.ldo.validation.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.app.helper.JsonHelper;

public class TestSetHelper {

    /**
     * Load the test set from the specified path in the configuration.
     * 
     * @return JsonNode representing the test set
     * @throws Exception if there is an error loading the test set
     */
    public static JsonNode loadTestSet() throws Exception {
        // AppStore.getInstance().getTestSetPath();
        var testSetPath = AppStore.getInstance().getTestSetPath();
        if (testSetPath == null || testSetPath.isEmpty()) {
            throw new IllegalArgumentException("Test set path is not set in the configuration.");
        }
        // load JSON file from the test set path
        var jsonNode = JsonHelper.readFileAsJsonNode(testSetPath);
        if (jsonNode == null) {
            throw new IllegalArgumentException("Test set is empty or not valid JSON.");
        }
        return jsonNode;
    }

}
