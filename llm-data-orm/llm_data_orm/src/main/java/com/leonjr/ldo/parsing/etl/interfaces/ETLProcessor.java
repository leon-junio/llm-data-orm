package com.leonjr.ldo.parsing.etl.interfaces;

import com.leonjr.ldo.parsing.llm.prompts.PromptTexts;

import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.service.SystemMessage;

public interface ETLProcessor {
    @SystemMessage(PromptTexts.ETL_PROCESS_TEXT)
    String process(ChatRequest chatRequest);

    @SystemMessage(PromptTexts.ETL_PROCESS_IMAGE)
    String processWithImage(ChatRequest chatRequest);

    @SystemMessage(PromptTexts.PRE_SUMMARIZE)
    String preSummarize(ChatRequest chatRequest);

    @SystemMessage(PromptTexts.IMAGE_SUMMARY_PROMPT)
    String imageSummary(ChatRequest chatRequest);
}
