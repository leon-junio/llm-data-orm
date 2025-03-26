package com.leonjr.ldo.extractor.utils;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageUtils {

    public static final String[] imageExtensions = new String[] { "jpg", "jpeg", "png", "gif", "webp" };

    public static boolean isImage(String path) {
        for (String extension : imageExtensions) {
            if (path.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static String getImageExtension(String path) {
        for (String extension : imageExtensions) {
            if (path.toLowerCase().endsWith(extension)) {
                return extension;
            }
        }
        return null;
    }

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

    public static String imageToBase64(String path) throws Exception {
        if (!isImage(path)) {
            throw new Exception("The provided path is not an image!");
        }

        byte[] imageBytes = Files.readAllBytes(Paths.get(path));
        return Base64.getEncoder().encodeToString(imageBytes);
    }

}
