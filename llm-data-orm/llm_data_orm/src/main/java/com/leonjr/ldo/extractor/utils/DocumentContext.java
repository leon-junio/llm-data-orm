package com.leonjr.ldo.extractor.utils;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;

public class DocumentContext {

    /**
     * Extracts all available context information from a Document object.
     * This method combines both the document's metadata and its text content
     * into a single string representation.
     *
     * @param document the Document object from which to extract context information
     * @return a string containing the document's metadata followed by a line separator
     *         and the document's text content
     */
    public static String getAllAvailableContextFromDocument(Document document) {
        var context = document.metadata().toString();
        context += System.lineSeparator();
        context += document.toTextSegment().toString();
        return context;
    }

    /**
     * Extracts all available context information from a text segment by combining
     * its metadata and text content.
     * 
     * @param segment the TextSegment from which to extract context information
     * @return a String containing the segment's metadata followed by a line separator
     *         and the segment's text content
     */
    public static String getAllAvailableContextFromSegment(TextSegment segment) {
        var context = segment.metadata().toString();
        context += System.lineSeparator();
        context += segment.text();
        return context;
    }

}
