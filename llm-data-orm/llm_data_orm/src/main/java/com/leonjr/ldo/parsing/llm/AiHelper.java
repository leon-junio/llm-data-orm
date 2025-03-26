package com.leonjr.ldo.parsing.llm;

import java.time.Duration;

import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.parsing.etl.interfaces.ETLProcessor;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.Data;

@Data
public class AiHelper {

    public static ETLProcessor buildNewAssistent(ChatLanguageModel chatModel) throws Exception {
        return AiServices.builder(ETLProcessor.class)
                .chatLanguageModel(chatModel).build();
    }

    public static ETLProcessor buildNewAssistent() throws Exception {
        var chatModel = getChatModel();
        return AiServices.builder(ETLProcessor.class)
                .chatLanguageModel(chatModel).build();
    }

    public static ChatLanguageModel getChatModel() throws IllegalArgumentException, Exception {
        var type = AppStore.getStartConfigs().getApp().getLlmType();
        switch (type) {
            case OPENAI:
                return getOpenAiChatLanguageModel();
            default:
                throw new IllegalArgumentException("Unsupported LLM type: " + type);
        }
    }

    public static ChatLanguageModel getOpenAiChatLanguageModel() throws Exception {
        return OpenAiChatModel.builder()
                .baseUrl(AppStore.getInstance().getLlmConfig().getOpenai().getCustomUrl())
                .apiKey(AppStore.getInstance().getLlmConfig().getOpenai().getApiKey())
                .modelName(AppStore.getInstance().getLlmConfig().getOpenai().getModelName().getModelName())
                .timeout(Duration.ofMinutes(10))
                .temperature(0.7)
                .maxTokens(4096)
                .build();
    }

}
