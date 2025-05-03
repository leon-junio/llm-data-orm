package com.leonjr.ldo.parsing.etl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.app.helper.LoggerHelper;
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

    public String preSummarize(String documentData) throws Exception {
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

    public String processChunkWithAiService(String chunk) throws Exception {
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

    public String executeParsing(List<TextSegment> chunks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(
                AppStore.getStartConfigs().getApp().getMaxExecutorsThreads());
        List<Future<String>> futures = new ArrayList<>();
        for (TextSegment chunk : chunks) {
            futures.add(executor.submit(
                    () -> processChunkWithAiService(
                            DocumentContext.getAllAvailableContextFromSegment(chunk))));
        }
        StringBuilder finalJson = new StringBuilder("[");
        for (Future<String> f : futures) {
            try {
                finalJson.append(f.get()).append(",");
            } catch (Exception e) {
                LoggerHelper.logger.error(
                        "Error while processing chunk: " + e.getMessage());
                throw e;
            }
        }
        if (finalJson.charAt(finalJson.length() - 1) == ',') {
            finalJson.deleteCharAt(finalJson.length() - 1);
        }
        finalJson.append("]");
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        return finalJson.toString();
    }

    public ResponseFormat getJsonResponseFormat() throws Exception {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .build();
        return responseFormat;
    }
}
