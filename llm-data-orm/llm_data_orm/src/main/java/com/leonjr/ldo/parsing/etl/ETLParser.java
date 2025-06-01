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
import com.leonjr.ldo.app.helper.JsonHelper;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.extractor.utils.DocumentContext;
import com.leonjr.ldo.parsing.llm.AiHelper;

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

    /**
     * Pre-processes and summarizes document data using AI assistance.
     * 
     * This method takes raw document data and generates a preliminary summary by
     * combining
     * it with table description information and sending it to an AI language model
     * for processing.
     * The response is formatted as JSON according to the specified response format.
     * 
     * @param documentData the raw text data from the document to be summarized
     * @return a string containing the AI-generated pre-summary of the document data
     * @throws Exception if there's an error during AI processing, message creation,
     *                   or communication with the language model
     */
    public String preSummarize(String documentData) throws Exception {
        UserMessage systemMessage = UserMessage.from(
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

    /**
     * Processes a data chunk using an AI service to transform it according to the
     * table structure.
     * 
     * <p>
     * This method applies rate limiting before processing and includes retry logic
     * with exponential
     * backoff to handle potential failures. The AI service receives both the table
     * structure description
     * and the data chunk to perform intelligent data transformation.
     * </p>
     * 
     * @param chunk the data chunk to be processed by the AI service
     * @return the processed result from the AI service as a String
     * @throws Exception if the processing fails after all retry attempts or if rate
     *                   limiting fails
     * 
     * @see #retryWithBackoff(Supplier, int, int)
     * @see AiHelper#buildNewAssistent()
     */
    public String processChunkWithAiService(String chunk) throws Exception {
        agentRateLimiter.acquire();
        return retryWithBackoff(() -> {
            var etlParserProcessor = AiHelper.buildNewAssistent();
            UserMessage userMessage = UserMessage.from(
                    "\"table_structure\":" + tableDescription + "\nchunk:" + chunk);
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(userMessage)
                    .responseFormat(getJsonResponseFormat())
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

    /**
     * Executes parallel parsing of text segments using AI service and aggregates
     * results into a single JSON array.
     * 
     * This method processes multiple text segments concurrently by submitting each
     * chunk to a thread pool
     * for AI-based parsing. The results are then cleaned, validated, and combined
     * into a unified JSON array.
     * 
     * @param chunks List of TextSegment objects to be processed and parsed
     * @return A JSON string containing an array of all successfully parsed and
     *         validated chunks
     * @throws Exception if any error occurs during chunk processing or if executor
     *                   termination fails
     * 
     * @implNote The method performs the following operations:
     *           <ul>
     *           <li>Creates a fixed thread pool using configured maximum executor
     *           threads</li>
     *           <li>Submits each chunk for processing with AI service using
     *           document context</li>
     *           <li>Cleans JSON responses by removing markdown formatting (```json,
     *           ```, `[]`)</li>
     *           <li>Extracts valid JSON arrays by trimming content outside bracket
     *           boundaries</li>
     *           <li>Validates each JSON chunk and skips invalid or non-array
     *           responses</li>
     *           <li>Concatenates all valid JSON arrays into a single result
     *           array</li>
     *           <li>Ensures proper executor shutdown with 1-minute timeout</li>
     *           </ul>
     * 
     *           Invalid chunks are logged and skipped rather than causing method
     *           failure.
     *           Debug information is logged when debug mode is enabled in AppStore.
     */
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
                String jsonParsed = f.get().replace("```json", "").replace("```", "").replace("`[]`", "[]");
                if (jsonParsed == null || jsonParsed.isEmpty()) {
                    continue;
                }

                // remove any text after last ] char
                int lastBracketIndex = jsonParsed.lastIndexOf(']');
                if (lastBracketIndex != -1) {
                    jsonParsed = jsonParsed.substring(0, lastBracketIndex + 1);
                }
                // remove any text before first [ char
                int firstBracketIndex = jsonParsed.indexOf('[');
                if (firstBracketIndex != -1) {
                    jsonParsed = jsonParsed.substring(firstBracketIndex);
                }

                int arrayStart = jsonParsed.indexOf('[');
                if (arrayStart == -1) {
                    continue;
                }

                jsonParsed = jsonParsed.substring(arrayStart);

                try {
                    var testJson = JsonHelper.convertJsonStringToJsonNode(jsonParsed);
                    if (testJson != null && !jsonParsed.trim().startsWith("[") && testJson.isArray()) {
                        System.out.println("CHUNK NOT ARRAY: " + jsonParsed);
                        continue;
                    }
                } catch (Exception e) {
                    LoggerHelper.logger.error(
                            "JSON CHUNK INVALID: " + e.getMessage());
                    continue;
                }

                finalJson.append(jsonParsed).append(",");
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

    /**
     * Creates and returns a JSON response format configuration.
     * 
     * This method constructs a ResponseFormat object configured for JSON output
     * using the builder pattern. The response format is set to JSON type.
     * 
     * @return ResponseFormat configured for JSON response type
     * @throws Exception if there's an error during ResponseFormat creation
     */
    public ResponseFormat getJsonResponseFormat() throws Exception {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .build();
        return responseFormat;
    }
}
