package com.leonjr.ldo.app.enums;

public enum OpenAiModels {
    GPT_3("gpt-3"), GPT_3_5("gpt-3.5"), GPT_4("gpt-4"), GPT_4O("gpt-4o"), GPT_4O_MINI("gpt-4o-mini"), GPT_O1("gpt-o1"),
    GPT_O1_MINI("gpt-o1-mini");

    private final String modelName;

    OpenAiModels(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }
}
