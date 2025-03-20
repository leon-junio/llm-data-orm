package com.leonjr.ldo.extractor.utils;

import dev.langchain4j.data.document.Document;

public class DocumentContext {

    public static String getAllAvailableContextFromDocument(Document document) {
        var context = document.metadata().toString();
        context += System.lineSeparator();
        context += document.toTextSegment().toString();
        return context;
    }

}
