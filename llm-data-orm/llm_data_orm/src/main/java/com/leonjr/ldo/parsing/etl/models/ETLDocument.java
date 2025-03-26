package com.leonjr.ldo.parsing.etl.models;

import java.awt.image.BufferedImage;
import java.util.List;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@Builder
@ToString(exclude = {"images", "segments", "document"})
public class ETLDocument {
    
    @NonNull
    private Document document;
    private List<BufferedImage> images;
    private List<TextSegment> segments;
    private String parsedResponse;

}
