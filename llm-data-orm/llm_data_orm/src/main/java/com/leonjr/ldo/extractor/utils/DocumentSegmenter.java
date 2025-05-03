package com.leonjr.ldo.extractor.utils;

import java.util.List;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByRegexSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;

public class DocumentSegmenter {

    private static final int LINES_MAX_SEGMENT_SIZE = 1024, DEFAULT_MAX_SEGMENT_SIZE = 512;
    private static final int MIN_SEGMENT_SIZE = 0;

    public static List<TextSegment> getSegments(Document document) throws Exception {
        String fileName = document.metadata().getString("file_name");
        String fileType = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        DocumentSplitter splitter;
        switch (fileType) {
            case "pdf":
            case "docx":
                splitter = new DocumentByLineSplitter(LINES_MAX_SEGMENT_SIZE, MIN_SEGMENT_SIZE);
                break;
            case "csv":
            case "tsv":
            case "txt":
                splitter = new DocumentByLineSplitter(LINES_MAX_SEGMENT_SIZE, MIN_SEGMENT_SIZE);
                break;
            case "json":
                splitter = new DocumentByRegexSplitter("\\{[^}]+\\}", "\n", DEFAULT_MAX_SEGMENT_SIZE, MIN_SEGMENT_SIZE);
                break;
            case "xml":
                splitter = new DocumentByRegexSplitter("<.*?>", " ", DEFAULT_MAX_SEGMENT_SIZE, MIN_SEGMENT_SIZE);
                break;
            case "xlsx":
                splitter = new DocumentByLineSplitter(DEFAULT_MAX_SEGMENT_SIZE, MIN_SEGMENT_SIZE);
                break;
            case "md":
                splitter = DocumentSplitters.recursive(DEFAULT_MAX_SEGMENT_SIZE, MIN_SEGMENT_SIZE);
                break;
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "webp":
                return List.of(TextSegment.from(document.text()));
            default:
                splitter = new DocumentByLineSplitter(DEFAULT_MAX_SEGMENT_SIZE, MIN_SEGMENT_SIZE);
                break;
        }
        return splitter.split(document);
    }
}
