package com.leonjr.ldo.parsing.llm;

import java.time.Duration;

import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.parsing.etl.interfaces.ETLProcessor;
import com.leonjr.ldo.parsing.etl.interfaces.LLMValidation;

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

    private static final double ETL_PROCESSING_TEMPERATURE = 0.2;
    private static final double ETL_PROCESSING_TOP_P = 1d;
    private static final double ETL_PROCESSING_FREQUENCY_PENALTY = 0d;
    private static final double ETL_PROCESSING_PRESENCE_PENALTY = 0d;
    private static final int ETL_PROCESSING_MAX_TOKENS = 12000;
    private static final int ETL_PROCESSING_TIMEOUT_MINUTES = 10;
    private static final double SUMMARY_MODEL_TEMPERATURE = 0.5;
    private static final double SUMMARY_MODEL_TOP_P = 0.9;
    private static final double SUMMARY_MODEL_FREQUENCY_PENALTY = 0.4;
    private static final double SUMMARY_MODEL_PRESENCE_PENALTY = 0.4;
    private static final int SUMMARY_MODEL_MAX_TOKENS = 12000;
    private static final int SUMMARY_MODEL_TIMEOUT_MINUTES = 10;

    /**
     * Builds a new AI assistant instance for ETL (Extract, Transform, Load)
     * processing operations.
     * 
     * This method creates an ETLProcessor instance using the AI Services framework,
     * configured
     * with the provided chat language model for natural language processing
     * capabilities.
     * 
     * @param chatModel the chat language model to be used by the ETL processor for
     *                  AI-powered
     *                  data processing and transformation operations
     * @return a new ETLProcessor instance configured with the specified chat model
     * @throws Exception if there is an error during the AI service builder
     *                   configuration
     *                   or ETLProcessor instantiation process
     */
    public static ETLProcessor buildNewAssistent(ChatLanguageModel chatModel) throws Exception {
        return AiServices.builder(ETLProcessor.class)
                .chatLanguageModel(chatModel).build();
    }

    /**
     * Creates and builds a new AI assistant instance for ETL (Extract, Transform,
     * Load) processing.
     * 
     * This method constructs an ETLProcessor using the AiServices builder pattern,
     * configured
     * with a chat language model obtained from the getChatModel() method.
     * 
     * @return ETLProcessor a configured AI assistant instance ready for ETL
     *         operations
     * @throws Exception if there's an error during the chat model retrieval or
     *                   service building process
     */
    public static ETLProcessor buildNewAssistent() throws Exception {
        var chatModel = getChatModel();
        return AiServices.builder(ETLProcessor.class)
                .chatLanguageModel(chatModel).build();
    }

    /**
     * Creates and configures a new LLMValidation instance using AI services.
     * 
     * This method builds a validator that leverages a chat language model to perform
     * validation operations. The validator is constructed using the AiServices builder
     * pattern with the configured chat model obtained from getChatModel().
     * 
     * @return a new LLMValidation instance configured with the chat language model
     * @throws Exception if there's an error during chat model retrieval or validator construction
     */
    public static LLMValidation buildNewValidator() throws Exception {
        var chatModel = getChatModel();
        return AiServices.builder(LLMValidation.class)
                .chatLanguageModel(chatModel).build();
    }

    /**
     * Creates and returns a ChatLanguageModel instance based on the configured LLM
     * type.
     * 
     * This method acts as a factory for creating different types of chat language
     * models
     * by reading the LLM type from the application configuration and instantiating
     * the
     * appropriate model implementation.
     * 
     * @return ChatLanguageModel instance configured according to the application
     *         settings
     * @throws IllegalArgumentException if the configured LLM type is not supported
     * @throws Exception                if there's an error during model creation or
     *                                  configuration retrieval
     */
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

    /**
     * Creates and configures an OpenAI chat language model with ETL processing
     * parameters.
     * 
     * This method builds an OpenAI chat model using configuration values from the
     * application
     * store, including API credentials, model settings, and processing parameters
     * optimized
     * for ETL (Extract, Transform, Load) operations.
     * 
     * @return A configured {@link ChatLanguageModel} instance ready for OpenAI API
     *         interactions
     * @throws Exception if there's an error during model configuration or if
     *                   required
     *                   configuration values are missing or invalid
     */
    public static ChatLanguageModel getOpenAiChatLanguageModel() throws Exception {
        return OpenAiChatModel.builder()
                .baseUrl(AppStore.getInstance().getLlmConfig().getOpenai().getCustomUrl())
                .apiKey(AppStore.getInstance().getLlmConfig().getOpenai().getApiKey())
                .modelName(AppStore.getInstance().getLlmConfig().getOpenai().getModelName().getModelName())
                .timeout(Duration.ofMinutes(ETL_PROCESSING_TIMEOUT_MINUTES))
                .temperature(ETL_PROCESSING_TEMPERATURE)
                .topP(ETL_PROCESSING_TOP_P)
                .frequencyPenalty(ETL_PROCESSING_FREQUENCY_PENALTY)
                .presencePenalty(ETL_PROCESSING_PRESENCE_PENALTY)
                .maxTokens(ETL_PROCESSING_MAX_TOKENS)
                .build();
    }

    /**
     * Creates and configures a generic chat language model using OpenAI
     * API-compatible settings.
     * 
     * This method builds an OpenAI chat model with configuration parameters
     * retrieved from
     * the application store, including custom URL, API key, model name, and various
     * ETL
     * processing parameters such as timeout, temperature, and token limits.
     * 
     * @return ChatLanguageModel a configured OpenAI chat model instance ready for
     *         use
     * @throws Exception if there's an error accessing configuration or building the
     *                   model
     */
    public static ChatLanguageModel getGenericChatLanguageModel() throws Exception {
        return OpenAiChatModel.builder() // Generic LLM must use OpenAiChatModel api models
                .baseUrl(AppStore.getInstance().getLlmConfig().getGenericAi().getCustomUrl())
                .apiKey(AppStore.getInstance().getLlmConfig().getGenericAi().getApiKey())
                .modelName(AppStore.getInstance().getLlmConfig().getGenericAi().getModelName())
                .timeout(Duration.ofMinutes(ETL_PROCESSING_TIMEOUT_MINUTES))
                .temperature(ETL_PROCESSING_TEMPERATURE)
                .topP(ETL_PROCESSING_TOP_P)
                .frequencyPenalty(ETL_PROCESSING_FREQUENCY_PENALTY)
                .presencePenalty(ETL_PROCESSING_PRESENCE_PENALTY)
                .maxTokens(ETL_PROCESSING_MAX_TOKENS)
                .build();
    }

    /**
     * Retrieves a configured ChatLanguageModel instance for AI summary operations.
     * The specific implementation returned depends on the LLM type configured in
     * the application settings.
     * 
     * @return a ChatLanguageModel instance configured for summary operations
     * @throws Exception                if there's an error creating the language
     *                                  model or if the configured LLM type is
     *                                  unsupported
     * @throws IllegalArgumentException if the LLM type specified in configuration
     *                                  is not supported
     */
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

    /**
     * Creates and configures an OpenAI chat language model specifically optimized
     * for text summarization tasks.
     * 
     * This method builds an OpenAI chat model with predefined parameters suitable
     * for generating
     * summaries, including specific temperature, token limits, and penalty settings
     * to ensure
     * concise and relevant output.
     * 
     * @return A configured {@link ChatLanguageModel} instance ready for
     *         summarization operations
     * @throws Exception if there's an error accessing the LLM configuration from
     *                   AppStore,
     *                   or if the OpenAI model configuration is invalid
     */
    public static ChatLanguageModel getOpenAiSummaryLanguageModel() throws Exception {
        return OpenAiChatModel.builder()
                .baseUrl(AppStore.getInstance().getLlmConfig().getOpenai().getCustomUrl())
                .apiKey(AppStore.getInstance().getLlmConfig().getOpenai().getApiKey())
                .modelName(AppStore.getInstance().getLlmConfig().getOpenai().getModelName().getModelName())
                .timeout(Duration.ofMinutes(SUMMARY_MODEL_TIMEOUT_MINUTES))
                .temperature(SUMMARY_MODEL_TEMPERATURE)
                .topP(SUMMARY_MODEL_TOP_P)
                .frequencyPenalty(SUMMARY_MODEL_FREQUENCY_PENALTY)
                .presencePenalty(SUMMARY_MODEL_PRESENCE_PENALTY)
                .maxTokens(SUMMARY_MODEL_MAX_TOKENS)
                .build();
    }

    /**
     * Creates and configures a generic AI language model specifically optimized for
     * text summarization tasks.
     * 
     * <p>
     * This method builds an OpenAI-compatible chat model using configuration
     * parameters from the
     * application store. The model is configured with specific timeout,
     * temperature, and token limits
     * suitable for summarization operations.
     * </p>
     * 
     * <p>
     * The model configuration includes:
     * </p>
     * <ul>
     * <li>Custom base URL and API key from the generic AI configuration</li>
     * <li>Predefined timeout of {@value #SUMMARY_MODEL_TIMEOUT_MINUTES}
     * minutes</li>
     * <li>Temperature, topP, frequency penalty, and presence penalty settings
     * optimized for summarization</li>
     * <li>Maximum token limit defined by {@value #SUMMARY_MODEL_MAX_TOKENS}</li>
     * </ul>
     * 
     * @return a configured {@link ChatLanguageModel} instance ready for text
     *         summarization tasks
     * @throws Exception if there's an error accessing the configuration or building
     *                   the model
     * @see AppStore#getLlmConfig()
     * @see OpenAiChatModel.Builder
     */
    public static ChatLanguageModel getGenericAiSummaryLanguageModel() throws Exception {
        return OpenAiChatModel.builder() // Generic LLM must use OpenAiChatModel api models
                .baseUrl(AppStore.getInstance().getLlmConfig().getGenericAi().getCustomUrl())
                .apiKey(AppStore.getInstance().getLlmConfig().getGenericAi().getApiKey())
                .modelName(AppStore.getInstance().getLlmConfig().getGenericAi().getModelName())
                .timeout(Duration.ofMinutes(SUMMARY_MODEL_TIMEOUT_MINUTES))
                .temperature(SUMMARY_MODEL_TEMPERATURE)
                .topP(SUMMARY_MODEL_TOP_P)
                .frequencyPenalty(SUMMARY_MODEL_FREQUENCY_PENALTY)
                .presencePenalty(SUMMARY_MODEL_PRESENCE_PENALTY)
                .maxTokens(SUMMARY_MODEL_MAX_TOKENS)
                .build();
    }

    /**
     * Generates a generic summary of an image using OpenAI's vision model.
     * 
     * This method processes a base64-encoded image and returns a JSON-formatted
     * summary
     * using the configured OpenAI chat model with vision capabilities.
     * 
     * @param base64Image the base64-encoded string representation of the image to
     *                    be summarized
     * @param mimeType    the MIME type of the image (e.g., "image/jpeg",
     *                    "image/png")
     * @return a JSON-formatted string containing the image summary generated by the
     *         AI model
     * @throws IllegalArgumentException if base64Image or mimeType is null
     * @throws Exception                if there's an error during the AI processing
     *                                  or network communication
     * 
     * @see OpenAiChatModel
     * @see ImageContent
     * @see ResponseFormat
     */
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
