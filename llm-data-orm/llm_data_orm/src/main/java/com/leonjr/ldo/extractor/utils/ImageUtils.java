package com.leonjr.ldo.extractor.utils;

public class ImageUtils {

    public static final String[] imageExtensions = new String[] { "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp",
            "avif", "tiff", "tif", "ico" };

    public static boolean isImage(String path) {
        for (String extension : imageExtensions) {
            if (path.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

}
