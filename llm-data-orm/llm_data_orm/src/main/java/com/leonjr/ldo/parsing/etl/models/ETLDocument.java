package com.leonjr.ldo.parsing.etl.models;

import java.awt.image.BufferedImage;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@Builder
@ToString(exclude = { "images", "segments", "document" })
public class ETLDocument {

    @NonNull
    private Document document;
    private List<BufferedImage> images;
    private List<TextSegment> segments;
    private String parsedResponse;

    /**
     * Parses the stored response string into a JsonNode representation.
     * 
     * This method takes the parsedResponse field (assumed to be a JSON string)
     * and converts it into a JsonNode object using Jackson's ObjectMapper.
     * 
     * @return JsonNode representation of the parsed response
     * @throws Exception if the parsedResponse contains invalid JSON or if parsing
     *                   fails
     */
    public JsonNode getJsonSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode parsedResponseJson = null;
        parsedResponseJson = mapper.readTree(parsedResponse);
        return parsedResponseJson;
    }

}
