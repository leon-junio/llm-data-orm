package com.leonjr.ldo.extractor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;

import org.apache.tika.parser.AutoDetectParser;

import com.leonjr.ldo.AppStore;
import com.leonjr.ldo.extractor.utils.DocumentSegmenter;
import com.leonjr.ldo.extractor.utils.HtmlCleaner;
import com.leonjr.ldo.extractor.utils.ImageUtils;
import com.leonjr.ldo.extractor.utils.PageExtractor;
import com.leonjr.ldo.extractor.utils.TextCleaner;
import com.leonjr.ldo.parsing.llm.AiHelper;

import dev.langchain4j.data.segment.TextSegment;

public final class DocumentTextExtractor {
    /**
     * Extracts and processes documents from a given file path or directory.
     * 
     * This method handles various document types including images, HTML files, and
     * other text-based documents.
     * It can process either a single file or recursively load all documents from a
     * directory.
     * 
     * @param path The file path or directory path to process. Can be either:
     *             - A directory path: All documents will be loaded recursively
     *             - A single file path: The specific file will be processed
     * 
     * @return A List of Document objects containing the extracted and cleaned text
     *         content
     *         along with their metadata. Each document represents either:
     *         - A single processed file (when path is a file)
     *         - Multiple documents (when path is a directory)
     * 
     * @throws Exception If any error occurs during:
     *                   - File system access or reading
     *                   - Document parsing with Apache Tika
     *                   - Image processing and AI summarization
     *                   - Page extraction for PDF files
     *                   - Text cleaning operations
     * 
     * @implNote The method performs the following processing steps:
     *           - For directories: Recursively loads all documents using Apache
     *           Tika parser
     *           - For images: Converts to base64, generates AI summary, and creates
     *           document
     *           - For PDFs: Extracts specific pages if page selection is enabled
     *           - For HTML files: Applies HTML-specific cleaning
     *           - For other files: Applies general text cleaning
     */
    public static List<Document> getDocument(String path) throws Exception {
        boolean isFolder = new File(path).isDirectory();
        DocumentParser parser = new ApacheTikaDocumentParser(AutoDetectParser::new, null, null, null, true);
        if (isFolder) {
            return FileSystemDocumentLoader.loadDocumentsRecursively(path, parser);
        } else {
            if (ImageUtils.isImage(path)) {
                var imageAsBase64 = ImageUtils.imageToBase64(path);
                var imageSummary = AiHelper.genericImageSummary(imageAsBase64, Files.probeContentType(Paths.get(path)));
                var doc = ImageUtils.createDocumentFromImagePath(path, imageSummary);
                return Arrays.asList(doc);
            }
            if (AppStore.getInstance().isPagePicksOrRange()) {
                File tempFilePath = PageExtractor.extractPageToTempFile(path);
                path = tempFilePath.getAbsolutePath();
            }
            String fileType = path.substring(path.lastIndexOf('.') + 1).toLowerCase();
            var rawDoc = FileSystemDocumentLoader.loadDocument(path, parser);
            String cleanedText = rawDoc.text();
            if (fileType.equals("html") || fileType.equals("htm")) {
                System.out.println("cleanedText: " + cleanedText);
                cleanedText = HtmlCleaner.cleanHtml(rawDoc.text());
                System.out.println("cleanedText: " + cleanedText);
            } else {
                cleanedText = TextCleaner.cleanText(cleanedText);
            }
            System.out.println("cleanedText3: " + cleanedText);
            Document cleanedDoc = Document.document(cleanedText, rawDoc.metadata());
            return Arrays.asList(cleanedDoc);
        }
    }

    /**
     * Extracts text segments from a document using the DocumentSegmenter.
     * 
     * This method serves as a facade to the DocumentSegmenter's getSegments method,
     * providing a convenient way to segment a document into smaller text portions
     * for further processing or analysis.
     * 
     * @param document the Document object to be segmented into text portions
     * @return a List of TextSegment objects representing the segmented portions of the document
     * @throws Exception if an error occurs during the document segmentation process
     */
    public static List<TextSegment> getSegments(Document document) throws Exception {
        return DocumentSegmenter.getSegments(document);
    }

}
