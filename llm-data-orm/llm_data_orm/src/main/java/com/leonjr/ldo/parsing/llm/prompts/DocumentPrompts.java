package com.leonjr.ldo.parsing.llm.prompts;

public enum DocumentPrompts {
    PRE_SUMMARIZE(PromptTexts.PRE_SUMMARIZE),
    ETL_PROCESS(PromptTexts.ETL_PROCESS_TEXT),
    VALIDATE_LLM_OUTPUT(PromptTexts.VALIDATE_LLM_OUTPUT);

    private final String prompt;

    DocumentPrompts(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }
}
