package com.leonjr.ldo.parsing.etl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.extractor.utils.DocumentContext;
import com.leonjr.ldo.parsing.etl.interfaces.ETLProcessor;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ETLParser {

    @NonNull
    private String tableDescription;
    @NonNull
    private ETLProcessor etlProcessor;
    @Builder.Default
    private ExecutorService threadsExecutor = Executors
            .newFixedThreadPool(AppStore.getStartConfigs().getApp().getMaxExecutorsThreads());
    @Builder.Default
    private List<Future<String>> openaiResults = new ArrayList<>();

    public String processChunkWithAiService(String chunk) {
        SystemMessage systemMessage = SystemMessage.from(
                "Table_structure" + tableDescription + "\n chunk: "
                        + chunk);
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .build();
        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(responseFormat)
                .messages(systemMessage)
                .build();
        var response = etlProcessor.process(chatRequest);
        return response;
    }

    public String preSummarize(String documentData) {
        SystemMessage systemMessage = SystemMessage.from(
                "Table description" + tableDescription + "\n Text: "
                        + documentData);
        ResponseFormat jsonFormat = getJsonResponseFormat();
        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(jsonFormat)
                .messages(systemMessage)
                .build();
        var response = etlProcessor.preSummarize(chatRequest);
        return response;
    }

    public String executeParsing(List<TextSegment> chunks) {
        for (TextSegment chunk : chunks) {
            openaiResults.add(threadsExecutor
                    .submit(() -> processChunkWithAiService(DocumentContext.getAllAvailableContextFromSegment(chunk))));
        }
        StringBuilder finalJson = new StringBuilder("[");
        for (Future<String> result : openaiResults) {
            try {
                finalJson.append(result.get()).append(",");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalJson.deleteCharAt(finalJson.lastIndexOf(","));
        threadsExecutor.shutdown();
        finalJson.append("]");
        return finalJson.toString();
    }

    public ResponseFormat getJsonResponseFormat() {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .build();
        return responseFormat;
    }
}
