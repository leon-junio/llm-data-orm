package com.leonjr.ldo.parsing.etl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.RateLimiter;
import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.extractor.utils.DocumentContext;
import com.leonjr.ldo.parsing.llm.AiHelper;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
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

    private static final int REQUESTS_PER_SECOND = 7;
    private static final RateLimiter agentRateLimiter = RateLimiter.create(REQUESTS_PER_SECOND);

    @NonNull
    private String tableDescription;

    public String preSummarize(String documentData) throws Exception {
        SystemMessage systemMessage = SystemMessage.from(
                "Table description" + tableDescription + "\n Text: "
                        + documentData);
        ResponseFormat jsonFormat = getJsonResponseFormat();
        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(jsonFormat)
                .messages(systemMessage)
                .build();
        var etlSummaryProcessor = AiHelper.buildNewAssistent(AiHelper.getAiSummaryLanguageModel());
        var response = etlSummaryProcessor.preSummarize(chatRequest);
        return response;
    }

    public String processChunkWithAiService(String chunk) throws Exception {
        agentRateLimiter.acquire();
        return retryWithBackoff(() -> {
            var etlParserProcessor = AiHelper.buildNewAssistent();
            UserMessage userMessage = UserMessage.from(
                    "\"table_structure\":" + tableDescription + "\nchunk: " + chunk);
            ChatRequest chatRequest = ChatRequest.builder()
                    .responseFormat(ResponseFormat.builder()
                            .type(ResponseFormatType.JSON).build())
                    .messages(userMessage)
                    .build();
            return etlParserProcessor.process(chatRequest);
        }, 5, 600);
    }

    /**
     * Tenta executar a callable; em caso de exceção, faz retry com backoff
     * exponencial.
     *
     * @param action       a chamada que pode falhar
     * @param maxRetries   número máximo de tentativas
     * @param initialDelay atraso inicial em ms
     */
    private <T> T retryWithBackoff(Callable<T> action, int maxRetries, long initialDelay) throws Exception {
        long delay = initialDelay;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return action.call();
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    throw e;
                }
                // opcional: logar e aguardar antes de tentar de novo
                LoggerHelper.logger.warn("Attempt " + attempt + " falhou: " + e.getMessage()
                        + ". Retrying in " + delay + "ms...");
                Thread.sleep(delay);
                delay *= 2; // backoff exponencial
            }
        }
        throw new IllegalStateException("Não deveria chegar aqui");
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
        if (AppStore.getInstance().isDebugAll()) {
            LoggerHelper.logger.info("Final JSON:\n"
                    + finalJson.toString().replace("```json", "").replace("```", "").replace("`[]`", "[]"));
        }
        return finalJson.toString().replace("```json", "").replace("```", "").replace("`[]`", "[]");
    }

    public ResponseFormat getJsonResponseFormat() throws Exception {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .build();
        return responseFormat;
    }
}
