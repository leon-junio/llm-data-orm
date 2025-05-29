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

    public static List<TextSegment> getSegments(Document document) throws Exception {
        return DocumentSegmenter.getSegments(document);
    }

}
