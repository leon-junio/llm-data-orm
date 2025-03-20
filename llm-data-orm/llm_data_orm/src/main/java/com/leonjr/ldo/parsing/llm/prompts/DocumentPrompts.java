package com.leonjr.ldo.parsing.llm.prompts;

public enum DocumentPrompts {
    PRE_SUMARIZE(
            "You should provide a simple summary of the document. This will be used to generate the document's metadata. You should ONLY provide a summary of the document's content, not the document's title or author. Inlcude important details (as table columns, titles and descriptions). The output should be a simple text with a paragraph describing the document's content. The output must be a single paragraph in english and should be the only content in the response."),
    TEST("This is a test prompt. Please only provide LOD - LLM ORM as response."),
    ETL_PROCESS(
            "You will receive a JSON with the table description of an model. You will receive a text with data from a text document and you should provide a JSON with the data extracted from the text document. The output should be a well structed based on the table description. This should be the only content in the response. The output must have columns in the same order as the table description and the data must be in the same order as the columns."),;

    private final String prompt;

    DocumentPrompts(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }
}
