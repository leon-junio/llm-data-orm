package com.leonjr.ldo.extractor.utils;

public class TextCleaner {

    /**
     * Cleans the input text by removing lines that contain URLs or web-related
     * content.
     * 
     * This method processes the input string line by line and filters out any lines
     * that match patterns indicating the presence of:
     * - HTTP/HTTPS URLs (http:// or https://)
     * - WWW addresses (www.)
     * - Mailto links (mailto:)
     * - Domain names ending with .com, .org, .gov, or .br
     * 
     * @param input the text to be cleaned, may contain multiple lines separated by
     *              newline characters
     * @return a cleaned version of the input text with web-related lines removed,
     *         preserving the original line structure for non-matching lines
     */
    public static String cleanText(String input) {
        StringBuilder result = new StringBuilder();

        for (String line : input.split("\n")) {

            if (line.matches(".*(https?://|www\\.|mailto:|\\S+\\.com|\\S+\\.org|\\S+\\.gov|\\S+\\.br).*")) {
                continue;
            }

            result.append(line).append("\n");
        }

        return result.toString();
    }
}
