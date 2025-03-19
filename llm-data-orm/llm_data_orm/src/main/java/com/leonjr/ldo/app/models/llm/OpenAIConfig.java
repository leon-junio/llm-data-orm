package com.leonjr.ldo.app.models.llm;

import com.leonjr.ldo.app.enums.OpenAiModels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OpenAIConfig {
    @Null(message = "Custom URL - Should be a HTTP base URL - e.g. http://localhost:5000 or https://local/api")
    private String customUrl;
    @NotBlank(message = "API Key - Should be a valid OpenAI API key")
    private String apiKey;
    @NotBlank(message = "Model Name - Should be a valid OpenAI model name")
    private OpenAiModels modelName;
}
