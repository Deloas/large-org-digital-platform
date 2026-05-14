package org.largeorg.platform.knowledge.service.impl;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.largeorg.platform.knowledge.service.DocumentParsingService;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
public class DocumentParsingServiceImpl implements DocumentParsingService {

    @Override
    public String parse(byte[] fileBytes, String fileType) {
        return switch (fileType.toUpperCase()) {
            case "PDF" -> parsePdf(fileBytes);
            case "DOCX" -> parseDocx(fileBytes);
            case "TXT" -> parseTxt(fileBytes);
            default -> throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        };
    }

    private String parsePdf(byte[] fileBytes) {
        try (PDDocument doc = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(doc);
            return cleanText(text);
        } catch (Exception e) {
            throw new RuntimeException("PDF 解析失败: " + e.getMessage(), e);
        }
    }

    private String parseDocx(byte[] fileBytes) {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(fileBytes))) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            return cleanText(extractor.getText());
        } catch (Exception e) {
            throw new RuntimeException("DOCX 解析失败: " + e.getMessage(), e);
        }
    }

    private String parseTxt(byte[] fileBytes) {
        return cleanText(new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8));
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replace("\r\n", "\n")
                   .replace("\r", "\n")
                   .replaceAll("\n{4,}", "\n\n\n")
                   .trim();
    }
}
