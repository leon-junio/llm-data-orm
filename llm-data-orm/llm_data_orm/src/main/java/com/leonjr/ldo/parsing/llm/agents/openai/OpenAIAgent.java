package com.leonjr.ldo.parsing.llm.agents.openai;

import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.parsing.llm.prompts.DocumentPrompts;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class OpenAIAgent {
    private static final OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .baseUrl(AppStore.getInstance().getLlmConfig().getOpenai().getCustomUrl())
            .apiKey(AppStore.getInstance().getLlmConfig().getOpenai().getApiKey())
            .modelName(AppStore.getInstance().getLlmConfig().getOpenai().getModelName().getModelName())
            .temperature(0.7)
            .maxTokens(2048)
            .build();

    public static String testModel() {
        return chatModel.chat(DocumentPrompts.TEST.getPrompt());
    }

    public static String preSumarize(Document document) {
        SystemMessage systemMessage = SystemMessage.from(
                DocumentPrompts.PRE_SUMARIZE.getPrompt() + "\n Text: " + document.text());
        var response = chatModel.chat(systemMessage);
        return response.aiMessage().text();
    }

    public static String textImage() {
        UserMessage userMessage = UserMessage.from(
                TextContent.from("Extract a description (all context, data and information) from the image below:"),
                ImageContent.from(
                        "https://minhapaginainicial.com.br/wp-content/uploads/2017/06/modelo-gerador-de-recibo.jpg"));

        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder()
                        .name("image_description")
                        .rootElement(JsonObjectSchema.builder()
                                .addStringProperty("title", "should be the image possible title")
                                .addStringProperty("description",
                                        "should be the fully image description (all context, data and information)")
                                .required("title", "description")
                                .build())
                        .build())
                .build();
        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(responseFormat)
                .messages(userMessage)
                .build();
        var response = chatModel.chat(chatRequest);
        return response.aiMessage().text();
    }

    public static String testEtlProcess(Document document, String tableDescription) {
        SystemMessage systemMessage = SystemMessage.from(
                DocumentPrompts.ETL_PROCESS.getPrompt() + "\n Table description" + tableDescription + "\n Text: "
                        + document.text());
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .build();
        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(responseFormat)
                .messages(systemMessage)
                .build();
        var response = chatModel.chat(chatRequest);
        return response.aiMessage().text();
    }
}
