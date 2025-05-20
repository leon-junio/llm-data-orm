package com.leonjr.ldo.parsing.llm;

import java.time.Duration;

import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.parsing.etl.interfaces.ETLProcessor;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ImageContent.DetailLevel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
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
            case GENERIC:
                return getGenericChatLanguageModel();
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
                .temperature(0.2) // quase determinístico
                .topP(0.8) // recorta as caudas
                .frequencyPenalty(0.4) // evita repetições
                .presencePenalty(0.4)
                .maxTokens(12000)
                .build();
    }

    public static ChatLanguageModel getGenericChatLanguageModel() throws Exception {
        return OpenAiChatModel.builder() // custom route to openAi chat models and local servers
                .baseUrl(AppStore.getInstance().getLlmConfig().getGenericAi().getCustomUrl())
                .apiKey(AppStore.getInstance().getLlmConfig().getGenericAi().getApiKey())
                .modelName(AppStore.getInstance().getLlmConfig().getGenericAi().getModelName())
                .timeout(Duration.ofMinutes(10))
                .temperature(0.2) // quase determinístico
                .topP(0.8) // recorta as caudas
                .frequencyPenalty(0.4) // evita repetições
                .presencePenalty(0.4)
                .maxTokens(12000)
                .build();
    }

    public static ChatLanguageModel getAiSummaryLanguageModel() throws Exception {
        var llmType = AppStore.getStartConfigs().getApp().getLlmType();
        switch (llmType) {
            case OPENAI:
                return getOpenAiSummaryLanguageModel();
            case GENERIC:
                return getGenericAiSummaryLanguageModel();
            default:
                throw new IllegalArgumentException("Unsupported LLM type: " + llmType);
        }
    }

    public static ChatLanguageModel getOpenAiSummaryLanguageModel() throws Exception {
        return OpenAiChatModel.builder()
                .baseUrl(AppStore.getInstance().getLlmConfig().getOpenai().getCustomUrl())
                .apiKey(AppStore.getInstance().getLlmConfig().getOpenai().getApiKey())
                .modelName(AppStore.getInstance().getLlmConfig().getOpenai().getModelName().getModelName())
                .timeout(Duration.ofMinutes(10))
                .temperature(0.5)
                .topP(0.9) // recorta as caudas
                .frequencyPenalty(0.4)
                .presencePenalty(0.4)
                .maxTokens(10000)
                .build();
    }

    public static ChatLanguageModel getGenericAiSummaryLanguageModel() throws Exception {
        return OpenAiChatModel.builder() // custom route to openAi chat models and local servers
                .baseUrl(AppStore.getInstance().getLlmConfig().getGenericAi().getCustomUrl())
                .apiKey(AppStore.getInstance().getLlmConfig().getGenericAi().getApiKey())
                .modelName(AppStore.getInstance().getLlmConfig().getGenericAi().getModelName())
                .timeout(Duration.ofMinutes(10))
                .temperature(0.5)
                .topP(0.9) // recorta as caudas
                .frequencyPenalty(0.4)
                .presencePenalty(0.4)
                .maxTokens(10000)
                .build();
    }

    public static String genericImageSummary(String base64Image, String mimeType) throws Exception {
        if (base64Image == null || mimeType == null) {
            throw new IllegalArgumentException("base64Image and imageExtension must not be null");
        }
        var chatModel = OpenAiChatModel.builder()
                .baseUrl(AppStore.getInstance().getLlmConfig().getOpenai().getCustomUrl())
                .apiKey(AppStore.getInstance().getLlmConfig().getOpenai().getApiKey())
                .modelName(AppStore.getInstance().getLlmConfig().getOpenai().getModelName().getModelName())
                .timeout(Duration.ofMinutes(10))
                .temperature(0.8)
                .maxTokens(10000)
                .build();
        var etlProcessor = buildNewAssistent(chatModel);
        UserMessage userMessage = UserMessage.from(
                TextContent.from("Image to summarize: "),
                ImageContent.from(base64Image, mimeType, DetailLevel.LOW));
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .build();
        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(responseFormat)
                .messages(userMessage)
                .build();
        var response = etlProcessor.imageSummary(chatRequest);
        LoggerHelper.logger.info("Image summary response: " + response);
        return response;
    }

}
