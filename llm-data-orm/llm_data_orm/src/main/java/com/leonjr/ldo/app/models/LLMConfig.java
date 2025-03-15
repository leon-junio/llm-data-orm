package com.leonjr.ldo.app.models;

import com.leonjr.ldo.app.models.llm.LocalLLMConfig;

import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LLMConfig {
    @Null
    private LocalLLMConfig local;
}
