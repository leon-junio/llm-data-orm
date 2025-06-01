package com.leonjr.ldo.extractor.utils;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageUtils {

    public static final String[] imageExtensions = new String[] { "jpg", "jpeg", "png", "gif", "webp" };

    /**
     * Checks if a file is an image based on its file extension.
     * 
     * @param path the file path or filename to check
     * @return true if the file has an image extension, false otherwise
     */
    public static boolean isImage(String path) {
        for (String extension : imageExtensions) {
            if (path.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines the file extension of an image file based on its path.
     * 
     * This method checks if the given file path ends with any of the supported
     * image extensions (case-insensitive comparison).
     * 
     * @param path the file path to check for image extension
     * @return the matching image extension (including the dot) if found,
     *         or null if the path doesn't end with a recognized image extension
     */
    public static String getImageExtension(String path) {
        for (String extension : imageExtensions) {
            if (path.toLowerCase().endsWith(extension)) {
                return extension;
            }
        }
        return null;
    }

    /**
     * Creates a Document object from an image file path and its base64-encoded
     * data.
     * 
     * This method validates that the provided path corresponds to an image file and
     * that
     * the image data is not null, then constructs a Document with appropriate
     * metadata
     * including file type, path information, and MIME type.
     * 
     * @param path      the file path to the image file - must be a valid image file
     *                  path
     * @param imageData the base64-encoded string representation of the image
     *                  content
     * @return a Document object containing the image data and associated metadata
     * @throws Exception if the path is not an image file or if imageData is null
     * 
     * @see Document#document(String, Metadata)
     * @see Metadata#metadata(String, String)
     */
    public static Document createDocumentFromImagePath(String path, String imageData) throws Exception {
        if (!isImage(path)) {
            throw new Exception("The provided path is not an image!");
        }
        if (imageData == null) {
            throw new Exception("The provided image data is null!");
        }
        return Document.document(imageData,
                Metadata.metadata("type", "image")
                        .put("file_path", path)
                        .put(Document.ABSOLUTE_DIRECTORY_PATH,
                                path.substring(0, path.lastIndexOf(".")))
                        .put(Document.FILE_NAME,
                                path.substring(path.lastIndexOf(".") + 1))
                        .put("image", "true")
                        .put("mime_type", Files.probeContentType(Paths.get(path))));
    }

    /**
     * Converts an image file to its Base64 string representation.
     * 
     * @param path the file path to the image that needs to be converted
     * @return a Base64 encoded string representation of the image
     * @throws Exception if the provided path does not point to a valid image file
     *                   or if an I/O error occurs while reading the file
     */
    public static String imageToBase64(String path) throws Exception {
        if (!isImage(path)) {
            throw new Exception("The provided path is not an image!");
        }

        byte[] imageBytes = Files.readAllBytes(Paths.get(path));
        return Base64.getEncoder().encodeToString(imageBytes);
    }

}
