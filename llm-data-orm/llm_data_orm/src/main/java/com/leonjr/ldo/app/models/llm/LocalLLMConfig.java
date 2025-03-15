package com.leonjr.ldo.app.models.llm;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LocalLLMConfig {
    @NotBlank(message = "Local model API server URL is required - Should be a HTTP base URL - e.g. http://localhost:5000 or https://local/api")
    private String httpModelUrl;
    @NotBlank(message = "Conversation api path is required - Should be a HTTP GET path - e.g. /conversation or /model/chat")
    private String conversationApiPath;
}
