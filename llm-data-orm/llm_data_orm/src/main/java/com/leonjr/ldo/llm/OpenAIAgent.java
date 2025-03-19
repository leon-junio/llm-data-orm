package com.leonjr.ldo.llm;

import com.leonjr.ldo.AppStore;

import dev.langchain4j.model.openai.OpenAiChatModel;

public class OpenAIAgent {
    private static final OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .baseUrl(AppStore.getInstance().getLlmConfig().getOpenai().getCustomUrl())
            .apiKey(AppStore.getInstance().getLlmConfig().getOpenai().getApiKey())
            .modelName(AppStore.getInstance().getLlmConfig().getOpenai().getModelName().getModelName())
            .temperature(0.7)
            .build();

    public static String testModel() {
        return chatModel.chat("This is a test, how are you felling today?");
    }
}
