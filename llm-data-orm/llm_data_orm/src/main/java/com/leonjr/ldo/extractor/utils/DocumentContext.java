package com.leonjr.ldo.extractor.utils;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;

public class DocumentContext {

    public static String getAllAvailableContextFromDocument(Document document) {
        var context = document.metadata().toString();
        context += System.lineSeparator();
        context += document.toTextSegment().toString();
        return context;
    }

    public static String getAllAvailableContextFromSegment(TextSegment segment) {
        var context = segment.metadata().toString();
        context += System.lineSeparator();
        context += segment.text();
        return context;
    }

}
