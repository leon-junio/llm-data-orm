package com.leonjr.ldo.app.models;

import com.leonjr.ldo.app.enums.LLMType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AppConfig {
    @NotBlank(message = "The LLM type is required")
    private LLMType llmType;
    @Null
    private LLMConfig llmConfig;
}
