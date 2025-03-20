package com.leonjr.ldo.parsing.llm.prompts;

public enum DocumentPrompts {
    PRE_SUMARIZE(
            "You should provide a simple summary of the document. This will be used to generate the document's metadata. You should ONLY provide a summary of the document's content, not the document's title or author. Inlcude important details (as table columns, titles and descriptions). The output should be a simple text with a paragraph describing the document's content."),
    TEST("This is a test prompt. Please only provide LOD - LLM ORM as response.");

    private final String prompt;

    DocumentPrompts(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }
}
