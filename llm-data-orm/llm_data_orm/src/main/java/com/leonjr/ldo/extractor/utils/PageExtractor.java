package com.leonjr.ldo.extractor.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import com.leonjr.ldo.AppStore;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PageExtractor {

    /**
     * Extracts pages from a file and saves them to a temporary file.
     * 
     * @param filePath the path to the file from which pages are to be extracted
     * @return a temporary file containing the extracted pages
     * @throws Exception if an error occurs during extraction
     */
    public static File extractPageToTempFile(String filePath) throws Exception {
        String extension = getFileExtension(filePath).toLowerCase();
        switch (extension) {
            case "pdf":
                return extractPdfPagesToTemp(filePath);
            default:
                throw new UnsupportedOperationException("Unsupported file type: " + extension);
        }
    }

    /**
     * Extracts specified pages from a PDF file and saves them to a temporary file.
     * 
     * @param filePath the path to the PDF file
     * @return a temporary file containing the extracted pages
     * @throws Exception   if an error occurs during extraction
     * @throws IOException if an error occurs while reading or writing files
     */
    private static File extractPdfPagesToTemp(String filePath) throws Exception, IOException {
        try (PDDocument original = Loader.loadPDF(new File(filePath))) {
            List<Integer> pages = AppStore.getInstance().getPicksOrRangePages(original.getNumberOfPages());
            File tempFile = null;
            try (PDDocument pagesParsedDoc = new PDDocument()) {
                for (Integer pageNumber : pages) {
                    if (pageNumber < 1 || pageNumber > original.getNumberOfPages()) {
                        throw new IllegalArgumentException(
                                "Page number " + pageNumber + " is out of bounds for document with "
                                        + original.getNumberOfPages() + " pages.");
                    }
                    PDPage page = original.getPage(pageNumber - 1);
                    pagesParsedDoc.addPage(page);
                }
                tempFile = File.createTempFile("extracted_page_", ".pdf");
                tempFile.deleteOnExit();
                pagesParsedDoc.save(tempFile);
            }
            return tempFile;
        }
    }

    /**
     * Extracts the file extension from the given path.
     * 
     * @param path the file path from which to extract the extension
     * @return the file extension, or an empty string if no extension is found
     */
    private static String getFileExtension(String path) {
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < path.length() - 1) {
            return path.substring(dotIndex + 1);
        }
        return "";
    }
}
