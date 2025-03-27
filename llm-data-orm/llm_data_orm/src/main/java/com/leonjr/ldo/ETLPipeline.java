package com.leonjr.ldo;

import java.util.ArrayList;
import java.util.List;

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
        LoggerHelper.logger.info("Describing database table...");
        tableDescription = DBHelper
                .getTableDescription(AppStore.getInstance().getTableName());
        LoggerHelper.logger.info(tableDescription);
        LoggerHelper.logger.info("Table description in JSON format:");
        LoggerHelper.logger.info(tableDescription.toJson());
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
        etlAgentParser = ETLParser.builder().etlProcessor(etlAgentProcessor)
                .tableDescription(tableDescription.toJson()).build();
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
    private void validateAndSummarizeDocuments() throws Exception {
        LoggerHelper.logger.info("Validating and summarizing documents...");
        for (var etlDocument : rawDocuments) {
            var sumarized = etlAgentParser
                    .preSummarize(DocumentContext.getAllAvailableContextFromDocument(etlDocument.getDocument()));
            if (sumarized.trim().equalsIgnoreCase("INVALID_PARSING")) {
                LoggerHelper.logger.error("INVALID_PARSING Document! Document is not related with table selected: "
                        + etlDocument.getDocument().metadata().toString());
                if (AppStore.getStartConfigs().getApp().isStopIfInvalidatedDocument()) {
                    throw new RuntimeException(
                            "INVALID_PARSING Document! Could not proceed with the ETL process because stopIfInvalidatedDocument is setted as true. Could not determine if the document is related with the table selected: "
                                    + "document: " + rawDocuments.indexOf(etlDocument) + "table: "
                                    + AppStore.getInstance().getTableName());
                }
                continue;
            }
            etlDocument.getDocument().metadata().put("sumarized", sumarized);
            if (AppStore.getInstance().isDebugAll()) {
                LoggerHelper.logger.info("Document " + rawDocuments.indexOf(etlDocument) + ":");
                LoggerHelper.logger.info(sumarized);
            }
        }
        validatedDocuments = rawDocuments.stream().filter(d -> d.getDocument().metadata().containsKey("sumarized"))
                .toList();
        LoggerHelper.logger.info("Number of validated documents: " + validatedDocuments.size());
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
        LoggerHelper.logger.info("Starting segmentation and chunking process...");
        for (var etlDocument : validatedDocuments) {
            var documentsSegments = DocumentTextExtractor.getSegments(etlDocument.getDocument());
            LoggerHelper.logger.info("Number of segments found: " + documentsSegments.size());
            if (AppStore.getInstance().isDebugAll()) {
                LoggerHelper.logger.info("Document " + rawDocuments.indexOf(etlDocument) + ":");
                LoggerHelper.logger.info("Segments found:");
                for (var segment : documentsSegments) {
                    LoggerHelper.logger.info(segment);
                }
            }
            var parsedResponse = etlAgentParser.executeParsing(documentsSegments);
            var parsedResponseAsJson = JsonResponseTransformer.parseJson(parsedResponse);
            LoggerHelper.logger.info("Parsing response to document " + rawDocuments.indexOf(etlDocument) + ":");
            LoggerHelper.logger.info("Parsed response:" + System.lineSeparator() + parsedResponseAsJson);
            etlDocument.setParsedResponse(parsedResponseAsJson);
        }
        LoggerHelper.logger.info("Parsing process finished!");
    }

    // Test and validate parsing process steps
    public void validateETLWithLocalTests() throws Exception {
        for (var etlDocument : validatedDocuments) {
            var response = ETLValidation.validateParsingLocally(etlDocument.getDocument().text(),
                    etlDocument.getJsonSchema(),
                    tableDescription, etlDocument.getParsedResponse());
            LoggerHelper.logger.info("Document " + rawDocuments.indexOf(etlDocument) + ":");
            LoggerHelper.logger.info("Validation response:" + System.lineSeparator() + response);
        }
    }

    // Insert all validated and tested data into the database
    /**
     * 
     * 
     * 
     */

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
        for (var etlDocument : validatedDocuments) {
            LoggerHelper.logger.info("Document " + rawDocuments.indexOf(etlDocument) + ":");
            LoggerHelper.logger.info(etlDocument);
        }
        LoggerHelper.logger.info(
                "===================================================================================================");
    }
}
