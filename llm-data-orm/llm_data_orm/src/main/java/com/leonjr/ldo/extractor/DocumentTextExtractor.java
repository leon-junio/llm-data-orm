package com.leonjr.ldo.extractor;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;

public final class DocumentTextExtractor {
    public static List<Document> getDocument(String path) throws Exception {
        boolean isFolder = new File(path).isDirectory();
        if (isFolder) {
            return FileSystemDocumentLoader.loadDocumentsRecursively(path, new TextDocumentParser());
        } else {
            return Arrays.asList(FileSystemDocumentLoader.loadDocument(path, new TextDocumentParser()));
        }
    }

    public static List<TextSegment> getSegments(Document document) throws Exception {
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(512, 0);
        List<TextSegment> segments = splitter.split(document);
        return segments;
    }

}
