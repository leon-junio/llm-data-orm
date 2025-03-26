package com.leonjr.ldo.extractor;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;

import org.apache.tika.parser.AutoDetectParser;

import com.leonjr.ldo.extractor.utils.DocumentSegmenter;
import com.leonjr.ldo.extractor.utils.ImageUtils;

import dev.langchain4j.data.segment.TextSegment;

public final class DocumentTextExtractor {
    public static List<Document> getDocument(String path) throws Exception {
        boolean isFolder = new File(path).isDirectory();
        DocumentParser parser = new ApacheTikaDocumentParser(AutoDetectParser::new, null, null, null, true);
        if (isFolder) {
            return FileSystemDocumentLoader.loadDocumentsRecursively(path, parser);
        } else {
            if (ImageUtils.isImage(path)) {
                var doc = Document.document(path,
                        Metadata.metadata("type", "image")
                                .merge(Metadata.metadata("file_path", path)
                                        .merge(Metadata.metadata(Document.ABSOLUTE_DIRECTORY_PATH,
                                                path.substring(0, path.lastIndexOf(File.separator))))
                                        .merge(Metadata.metadata(Document.FILE_NAME,
                                                path.substring(path.lastIndexOf(File.separator) + 1)))));
                return Arrays.asList(doc);
            }
            return Arrays.asList(FileSystemDocumentLoader.loadDocument(path, parser));
        }
    }

    public static List<TextSegment> getSegments(Document document) throws Exception {
        return DocumentSegmenter.getSegments(document);
    }

}
