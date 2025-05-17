package com.leonjr.ldo.extractor.utils;

public class TextCleaner {

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
