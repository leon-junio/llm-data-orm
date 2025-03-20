package com.leonjr.ldo.parsing.llm.agents.openai;

import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.parsing.llm.prompts.DocumentPrompts;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class OpenAIAgent {
    private static final OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .baseUrl(AppStore.getInstance().getLlmConfig().getOpenai().getCustomUrl())
            .apiKey(AppStore.getInstance().getLlmConfig().getOpenai().getApiKey())
            .modelName(AppStore.getInstance().getLlmConfig().getOpenai().getModelName().getModelName())
            .temperature(0.7)
            .build();

    public static String testModel() {
        return chatModel.chat(DocumentPrompts.TEST.getPrompt());
    }

    public static String preSumarize(Document document) {
        SystemMessage systemMessage = SystemMessage.from(
                DocumentPrompts.PRE_SUMARIZE.getPrompt());
        UserMessage userMessage = UserMessage.from(
                TextContent.from(document.text()));
        var response = chatModel.chat(systemMessage, userMessage);
        return response.aiMessage().text();
    }

    public static String textImage() {
        UserMessage userMessage = UserMessage.from(
                TextContent.from("What do you see?"),
                ImageContent.from(
                        "https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png"));
        var response = chatModel.chat(userMessage);
        return response.aiMessage().text();
    }
}
