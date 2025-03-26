package com.leonjr.ldo.extractor;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;

import dev.langchain4j.data.document.Document;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;

public class DocumentImageExtractor {

    public static List<BufferedImage> extractImagesFromPDF(String pdfPath) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(new File(pdfPath))) {
            for (PDPage page : document.getPages()) {
                Iterable<PDImageXObject> imageObjects = StreamSupport
                        .stream(page.getResources().getXObjectNames().spliterator(), false)
                        .map(name -> {
                            try {
                                return (PDImageXObject) page.getResources().getXObject(name);
                            } catch (IOException | ClassCastException e) {
                                return null;
                            }
                        }).filter(obj -> obj != null).toList();

                for (PDImageXObject imageObject : imageObjects) {
                    images.add(imageObject.getImage());
                }
            }
        }
        return images;
    }

    public static List<BufferedImage> extractImagesFromDocx(String docxPath) throws Exception {
        List<BufferedImage> images = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(docxPath);
                XWPFDocument document = new XWPFDocument(OPCPackage.open(fis))) {
            for (XWPFPictureData picture : document.getAllPictures()) {
                ByteArrayInputStream bis = new ByteArrayInputStream(picture.getData());
                BufferedImage img = ImageIO.read(bis);
                images.add(img);
            }
        }
        return images;
    }

    public static List<BufferedImage> extractImagesFromPptx(String pptxPath) throws Exception {
        List<BufferedImage> images = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(pptxPath);
                XMLSlideShow ppt = new XMLSlideShow(OPCPackage.open(fis))) {
            for (XSLFPictureData picture : ppt.getPictureData()) {
                ByteArrayInputStream bis = new ByteArrayInputStream(picture.getData());
                BufferedImage img = ImageIO.read(bis);
                images.add(img);
            }
        }
        return images;
    }

    public static List<BufferedImage> extractImages(Document document) throws Exception {
        String filePath = document.metadata().getString(Document.ABSOLUTE_DIRECTORY_PATH) + File.separator
                + document.metadata().getString(Document.FILE_NAME);
        if (filePath.endsWith(".pdf")) {
            return extractImagesFromPDF(filePath);
        } else if (filePath.endsWith(".docx")) {
            return extractImagesFromDocx(filePath);
        } else if (filePath.endsWith(".pptx")) {
            return extractImagesFromPptx(filePath);
        }
        return null;
    }
}