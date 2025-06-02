package com.leonjr.ldo.app.models;

import com.leonjr.ldo.app.enums.LLMType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AppConfig {
    @NotBlank(message = "The LLM type is required")
    private LLMType llmType;

    @NotBlank(message = "The LLM validation type is required")
    private boolean validateLLMResultsWithLLM;

    @Null
    private LLMConfig llmConfig;

    @NotBlank(message = "MAX executors threads is required - default 10")
    private Integer maxExecutorsThreads = 10;

    @NotBlank(message = "MAX DB insertion chunk size is required - default 100")
    private Integer maxDBInsertionChunkSize = 100;

    @NotBlank(message = "MAX ETL processors is required - default 4")
    private Integer maxETLProcessors = 4;

    @NotNull(message = "Stop if invalidated document was found when parsing data from documents - default false")
    private boolean stopIfInvalidatedDocument = false;
}
