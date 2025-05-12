package com.leonjr.ldo.extractor.utils;

import java.util.List;
import java.util.Objects;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByRegexSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;

public class DocumentSegmenter {

    private static final int LINES_MAX_SEGMENT_SIZE = 1024, DEFAULT_MAX_SEGMENT_SIZE = 512;
    private static final int MIN_SEGMENT_SIZE = 0;

    /**
     * Splits a document into segments based on its file type.
     *
     * @param document the document to be split
     * @return a list of text segments
     * @throws Exception if an error occurs during splitting
     */
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
                splitter = new DocumentByLineSplitter(2048, 0);
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
        var segments = splitter.split(document);
        return removeEmptyOrNullSegments(segments);
    }

    /**
     * Removes empty or null segments from the list of segments.
     *
     * @param segments the list of segments to filter
     * @return a new list of segments without empty or null segments
     */
    public static List<TextSegment> removeEmptyOrNullSegments(List<TextSegment> segments) {
        return segments.stream()
                .filter(Objects::nonNull)
                .filter(segment -> {
                    String text = segment.text();
                    return text != null && !text.trim().isEmpty() && !text.matches("[-–—\\*\\s]*");
                })
                .toList();
    }
}
