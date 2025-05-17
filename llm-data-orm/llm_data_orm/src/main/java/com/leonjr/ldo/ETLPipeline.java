package com.leonjr.ldo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.leonjr.ldo.app.helper.LoggerHelper;
import com.leonjr.ldo.database.handler.DBHelper;
import com.leonjr.ldo.database.models.TableDescription;
import com.leonjr.ldo.extractor.DocumentImageExtractor;
import com.leonjr.ldo.extractor.DocumentTextExtractor;
import com.leonjr.ldo.extractor.utils.DocumentContext;
import com.leonjr.ldo.extractor.utils.JsonResponseTransformer;
import com.leonjr.ldo.parsing.etl.ETLParser;
import com.leonjr.ldo.parsing.etl.interfaces.ETLProcessor;
import com.leonjr.ldo.parsing.etl.models.ETLDocument;
import com.leonjr.ldo.parsing.llm.AiHelper;
import com.leonjr.ldo.validation.ETLValidation;
import com.leonjr.ldo.validation.helper.TestSetHelper;
import com.leonjr.ldo.validation.models.LocalSimpleValidationResult;

import ch.qos.logback.core.util.Duration;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Data
@ToString(callSuper = false, exclude = { "etlAgentProcessor", "tableDescription", "etlAgentParser", "rawDocuments",
        "validatedDocuments" })
public final class ETLPipeline {
    @NonNull
    private String fileOrFolderPath;
    private TableDescription tableDescription;
    private ETLProcessor etlAgentProcessor;
    private ETLParser etlAgentParser;
    private List<ETLDocument> rawDocuments;
    private List<ETLDocument> validatedDocuments;
    private long startExecutionTime, endExecutionTime;

    /**
     * Step 1: Extract table schema from database - This method will extract the
     * table schema from the database
     *
     * @throws Exception If an error occurs while extracting the table schema
     */
    private void describeDatabaseTable() throws Exception {
        long startTime = System.currentTimeMillis();
        LoggerHelper.logger.info("Describing database table...");
        tableDescription = DBHelper
                .getTableDescription(AppStore.getInstance().getTableName());
        LoggerHelper.logger.info(tableDescription);
        LoggerHelper.logger.info("Table description in JSON format:");
        LoggerHelper.logger.info(tableDescription.toJson());
        LoggerHelper.logger.info("Table description loaded successfully!");
        long endTime = System.currentTimeMillis();
        LoggerHelper.logger.info("Table description time: "
                + Duration.buildByMilliseconds(endTime - startTime));
    }

    /**
     * Step 2: Extract data from documents - This method will extract the data from
     * the documents. It include document text, metadata, segments and images
     *
     * @throws Exception If an error occurs while extracting the data from the
     *                   documents
     */
    private void extractDataFromDocuments() throws Exception {
        LoggerHelper.logger.info("Loading documents and extracting data...");
        long startTime = System.currentTimeMillis();
        rawDocuments = new ArrayList<>();
        var documents = DocumentTextExtractor.getDocument(fileOrFolderPath);
        LoggerHelper.logger.info("Documents loaded successfully!");
        for (var document : documents) {
            var foundedImages = DocumentImageExtractor.extractImages(document);
            if (foundedImages != null && !foundedImages.isEmpty()) {
                LoggerHelper.logger
                        .info("Found " + foundedImages.size() + " images in document " + documents.indexOf(document));
            }
            rawDocuments.add(ETLDocument.builder().document(document).images(foundedImages).build());
            if (AppStore.getInstance().isDebugAll()) {
                LoggerHelper.logger.info("Document " + documents.indexOf(document) + ":");
                LoggerHelper.logger.info(document);
            }
        }
        LoggerHelper.logger.info("Number of documents found at the path: " + rawDocuments.size());
        long endTime = System.currentTimeMillis();
        LoggerHelper.logger.info("Data extraction time: "
                + Duration.buildByMilliseconds(endTime - startTime));
        if (rawDocuments.isEmpty()) {
            LoggerHelper.logger.warn("No documents to run ETL Process!");
            throw new Exception("No documents found at the path: " + fileOrFolderPath);
        }
    }

    /**
     * Step 3: Start the ETL processor - This method will start the ETL processor
     * with LLM model
     * 
     * @throws Exception If an error occurs while starting the ETL processor
     */
    private void startETLProcessor() throws Exception {
        LoggerHelper.logger.info("Starting ETL Processor...");
        etlAgentProcessor = AiHelper.buildNewAssistent();
        etlAgentParser = ETLParser.builder().tableDescription(tableDescription.toJson()).build();
        LoggerHelper.logger.info("ETL Processor started successfully!");
    }

    /**
     * Step 4: Validate and summarize documents - This method will validate and
     * summarize the documents. Validate means that the document is related with the
     * table selected and summarize means that the document will be parsed to be
     * inserted into a database
     * 
     * @throws Exception If an error occurs while validating and summarizing the
     *                   documents or if the document is not related with the table
     *                   selected
     */
    private void validateAndSummarizeDocuments() throws InterruptedException, Exception {
        long startValidationTime = System.currentTimeMillis();
        LoggerHelper.logger.info("Validating and summarizing documents in parallel...");
        validatedDocuments = new ArrayList<>();

        int maxEtlProcessors = AppStore.getStartConfigs().getApp().getMaxETLProcessors();
        ExecutorService etlProcessors = Executors.newFixedThreadPool(maxEtlProcessors);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < rawDocuments.size(); i++) {
            final int index = i;
            ETLDocument etlDocument = rawDocuments.get(i);

            futures.add(etlProcessors.submit(() -> {
                try {
                    String context = DocumentContext.getAllAvailableContextFromDocument(etlDocument.getDocument());
                    String summarized = etlAgentParser.preSummarize(context);
                    etlDocument.getDocument().metadata().put("summarized", summarized);
                    if (AppStore.getInstance().isDebugAll()) {
                        LoggerHelper.logger.info("[Document " + index + "] Summarized: " + summarized);
                    }
                    if (summarized == null || summarized.isEmpty()) {
                        throw new Exception("Document summarized is null or empty");
                    }
                    if (summarized.trim().replace("\n", "").equalsIgnoreCase("INVALID_PARSING")) {
                        LoggerHelper.logger.error("[Document " + index
                                + "] Validation error found: Document is not related with the table selected!\nResponse: "
                                + summarized);
                        return null;
                    }
                    return index;
                } catch (Exception ex) {
                    LoggerHelper.logger.error("[Document " + index + "] Error during summarization: " + ex.getMessage(),
                            ex);
                    return null;
                }
            }));
        }

        List<ETLDocument> validDocs = new ArrayList<>();
        for (Future<Integer> future : futures) {
            try {
                Integer idx = future.get();
                if (idx != null) {
                    var rawDoc = rawDocuments.get(idx);
                    validDocs.add(rawDoc);
                } else {
                    LoggerHelper.logger.warn("Document invalidated found!");
                }
            } catch (ExecutionException e) {
                LoggerHelper.logger.error("Future execution error: " + e.getMessage(), e);
            }
        }

        etlProcessors.shutdown();
        etlProcessors.awaitTermination(1, TimeUnit.HOURS);

        validatedDocuments.addAll(validDocs);

        LoggerHelper.logger.info("Number of validated documents: " + validatedDocuments.size());
        LoggerHelper.logger.info("Number of invalid documents: " + (rawDocuments.size() - validatedDocuments.size()));

        if (AppStore.getStartConfigs().getApp().isStopIfInvalidatedDocument()
                && rawDocuments.size() - validatedDocuments.size() > 0) {
            LoggerHelper.logger.error("There are invalid documents, stopping execution!");
            throw new Exception(
                    "Stopping execution due to invalid documents found! Variable stopIfInvalidatedDocument is set to true. Invalid documents: "
                            + (rawDocuments.size() - validatedDocuments.size()));
        }

        long endValidationTime = System.currentTimeMillis();
        LoggerHelper.logger.info("Validation and summarization time: "
                + Duration.buildByMilliseconds(endValidationTime - startValidationTime));
    }

    /**
     * Step 5: Start parsing process - This method will start the parsing process
     * The first step is to segment the document and then chunk it to be parsed
     * After that, the ETL processor will execute the parsing process and return the
     * response in JSON format
     * 
     * @throws Exception If an error occurs while starting the parsing process
     */
    private void parsingProcess() throws Exception {
        long startParsingTime = System.currentTimeMillis();
        LoggerHelper.logger.info("Starting segmentation and chunking process in parallel...");

        int maxEtlProcessors = AppStore.getStartConfigs().getApp().getMaxETLProcessors();
        ExecutorService etlProcessors = Executors.newFixedThreadPool(maxEtlProcessors);
        List<Future<Integer>> futures = new ArrayList<>();

        if (validatedDocuments.isEmpty()) {
            LoggerHelper.logger.warn("No documents to parse!");
            return;
        }

        for (int i = 0; i < validatedDocuments.size(); i++) {
            final int index = i;
            ETLDocument etlDocument = validatedDocuments.get(i);
            futures.add(etlProcessors.submit(() -> {
                var segments = DocumentTextExtractor.getSegments(etlDocument.getDocument());
                LoggerHelper.logger.info("[Document " + index + "] Segments: " + segments.size());
                String parsedResponse = etlAgentParser.executeParsing(segments);
                String jsonResponse = JsonResponseTransformer.parseJson(parsedResponse);
                etlDocument.setParsedResponse(jsonResponse);
                LoggerHelper.logger.info("[Document " + index + "] Parsing completed.");
                return index;
            }));
        }

        // wait for all futures to complete
        for (Future<Integer> future : futures) {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                LoggerHelper.logger.error("Error in parallel parsing: " + e.getMessage(), e);
            }
        }

        etlProcessors.shutdown();
        etlProcessors.awaitTermination(1, TimeUnit.HOURS);

        LoggerHelper.logger.info("All documents parsed, total: " + validatedDocuments.size());
        long endParsingTime = System.currentTimeMillis();
        LoggerHelper.logger.info("Parsing time: " + Duration.buildByMilliseconds(endParsingTime - startParsingTime));
    }

    // Test and validate parsing process steps
    public void validateETLWithLocalTests() throws Exception {
        LoggerHelper.logger.info("Validating parsing process with local tests...");

        if (validatedDocuments.isEmpty()) {
            LoggerHelper.logger.warn("No documents to validate!");
            return;
        }

        LocalSimpleValidationResult validationResult = null;
        List<LocalSimpleValidationResult> testSetResults = new ArrayList<>();

        if (AppStore.getInstance().getTestSetPath() == null
                || AppStore.getInstance().getTestSetPath().isEmpty()) {
            LoggerHelper.logger.warn("Not found test Set data, using local validation!");
            for (var etlDocument : validatedDocuments) {
                validationResult = ETLValidation.validateParsingLocally(
                        etlDocument.getJsonSchema(),
                        tableDescription);
                LoggerHelper.logger.info("Document " + rawDocuments.indexOf(etlDocument) + ":");
                LoggerHelper.logger.info("Validation response:" + System.lineSeparator() + validationResult);
            }
        } else {
            JsonNode testSet = TestSetHelper.loadTestSet();
            // check if have the same size of documents and keys of testSet json array
            if (validatedDocuments.size() > testSet.size()) {
                LoggerHelper.logger.error("Test set size is less than the number of documents!");
                throw new Exception("Test set size is less than the number of documents!");
            }
            int index = 0;
            for (var etlDocument : validatedDocuments) {
                var test = testSet.get(index);
                index++;
                validationResult = ETLValidation.validateParsingWithTestJson(test,
                        etlDocument.getJsonSchema(), tableDescription);
                LoggerHelper.logger.info("Document " + rawDocuments.indexOf(etlDocument) + ":");
                LoggerHelper.logger.info("Validation response:" + System.lineSeparator() +
                        validationResult);
            }
        }

        // Force execution stop
        if (validationResult != null) {
            if (!validationResult.getMissingMandatoryFields().isEmpty()) {
                throw new Exception("Execution stopped due to missing mandatory fields: "
                        + validationResult.getMissingMandatoryFields());
            }
            if (!validationResult.getDataTypeErrors().isEmpty()) {
                throw new Exception("Execution stopped due to data type errors: "
                        + validationResult.getDataTypeErrors());
            }
            var conformity = validationResult.getConformityAndUnknownRate().getLeft();
            var unknownRate = validationResult.getConformityAndUnknownRate().getRight();
            if (conformity < 0.9) {
                throw new Exception("Execution stopped due to conformity rate: " + conformity);
            }
            if (unknownRate > 0.1) {
                throw new Exception("Execution stopped due to unknown rate: " + unknownRate);
            }
        } else {
            throw new Exception("Could not found any validation result!");
        }
    }

    /**
     * Step 6: Insert data into database - This method will insert the data into the
     * database
     * 
     * @throws Exception If an error occurs while inserting the data into the
     *                   database
     */
    public void insertETLIntoDatabase() throws Exception {
        LoggerHelper.logger.info("Inserting data into database...");
        if (validatedDocuments.isEmpty()) {
            LoggerHelper.logger.warn("No documents to insert!");
            return;
        }
        long startInsertTime = System.currentTimeMillis();
        for (var etlDocument : validatedDocuments) {
            try {
                if (DBHelper.insertParsedDocumentAtDatabase(
                        AppStore.getInstance().getTableName(),
                        tableDescription,
                        etlDocument.getJsonSchema())) {
                    LoggerHelper.logger
                            .info("Document " + rawDocuments.indexOf(etlDocument) + " inserted successfully!");
                } else {
                    LoggerHelper.logger.error("Error inserting document " + rawDocuments.indexOf(etlDocument) + "!");
                }
            } catch (SQLException e) {
                LoggerHelper.logger
                        .error("Error inserting document " + rawDocuments.indexOf(etlDocument) + ": " + e.getMessage());
            }
        }
        long endInsertTime = System.currentTimeMillis();
        LoggerHelper.logger.info("Data inserted into database successfully!");
        LoggerHelper.logger.info("Insertion time: " + Duration.buildByMilliseconds(endInsertTime - startInsertTime));
    }

    /**
     * Boot the ETL pipeline - This method will boot the ETL pipeline
     * 
     * @throws Exception If an error occurs while booting the ETL pipeline
     */
    public void boot() throws Exception {
        startExecutionTime = System.currentTimeMillis();
        // parsing pipeline steps
        describeDatabaseTable();
        extractDataFromDocuments();
        startETLProcessor();
        validateAndSummarizeDocuments();
        parsingProcess();
        // test parsing process
        /** */
        endExecutionTime = System.currentTimeMillis();
        // print results
        debugETLResults();
        // print tests and validations
        validateETLWithLocalTests();
        // validateETLWithLocalTests();
        // validateETLWithLocalTests();
        // insert data into database
        insertETLIntoDatabase();
    }

    /**
     * Debug ETL results - This method will print the ETL results
     */
    public void debugETLResults() {
        LoggerHelper.logger.info(
                "=========================================== ETL Results ===========================================");
        LoggerHelper.logger.info("Number of documents found: " + rawDocuments.size());
        LoggerHelper.logger.info("Number of validated documents: " + validatedDocuments.size());
        var etlDuration = Duration.buildByMilliseconds(endExecutionTime - startExecutionTime);
        LoggerHelper.logger.info("Execution time: " + etlDuration.toString());
        if (AppStore.getInstance().isDebugAll()) {
            for (var etlDocument : validatedDocuments) {
                LoggerHelper.logger.info("Document " + rawDocuments.indexOf(etlDocument) + ":");
                LoggerHelper.logger.info(etlDocument);
            }
        }
        LoggerHelper.logger.info(
                "===================================================================================================");
    }
}
