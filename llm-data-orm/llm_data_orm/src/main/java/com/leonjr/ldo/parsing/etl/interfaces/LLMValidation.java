package com.leonjr.ldo.parsing.etl.interfaces;

import com.leonjr.ldo.parsing.llm.prompts.PromptTexts;
import com.leonjr.ldo.validation.models.LLMValidationResult;

import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.service.SystemMessage;

public interface LLMValidation {
    @SystemMessage(PromptTexts.VALIDATE_LLM_OUTPUT)
    LLMValidationResult validate(ChatRequest chatRequest);
}
