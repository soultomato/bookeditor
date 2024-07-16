package com.soultomato.main.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class getBookContent {

    private static String endString = "\0";

    public static String reeditorText(String text) {
        String newText = "";
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            newText = text.replace(" \r\n \r\n", "").replace(" \r\n", "@@@@###").replace("\r\n", "").replace("@@@@###", "\r\n");
        }else {
            newText = text.replace(" \n \n", "").replace(" \n", "@@@@###").replace("\n", "").replace("@@@@###", "\n");
        }
        return newText;
    }

    public static String extractChapterContent(String pdfFilePath, String targetChapterTitle) {
        pdfFilePath = "../uploads/" + pdfFilePath;
        File file = new File(pdfFilePath);

        if (!file.exists()) {
            System.out.println("File does not exist: " + pdfFilePath);
            return null;
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            if (!document.isEncrypted()) {
                PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
                if (outline != null) {
                    PDOutlineItem item = outline.getFirstChild();
                    String result = extractSpecifiedChapterContent(document, item, targetChapterTitle, null);
                    if (result != null) {
                        result = extractBetween(result, targetChapterTitle, endString);
                        return result;
                    }
                } else {
                    System.out.println("This document does not contain any bookmarks.");
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the PDF file.");
            e.printStackTrace();
        }
        return "can't find chapter title";
    }

    private static String patternStr(String text, String title) {
        String regex = "(\\d+(\\.\\d+)*)(\\s*)(.*)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(title);

        if (matcher.matches()) {
            String newTitle = matcher.group(1) + "\\s+" +matcher.group(4);

            Pattern pattern1 = Pattern.compile(newTitle);
            Matcher matcher1 = pattern1.matcher(text);
            if (matcher1.find()) {
                return matcher1.group();
            }
        }
        return title;
    }

    private static String extractBetween(String target, String sub1, String sub2) {
        int startIndex = target.indexOf(sub1);
        int endIndex = target.indexOf(sub2, startIndex + sub1.length());

        if (startIndex == -1) {
            sub1 = patternStr(target, sub1);
        }
        if (endIndex == -1) {
            sub2 = patternStr(target, sub2);
        }
        startIndex = target.indexOf(sub1);
        endIndex = target.indexOf(sub2, startIndex + sub1.length());

        if (startIndex != -1 && endIndex > -1) {
            return target.substring(startIndex, endIndex).trim();
        } else if (startIndex != -1) {
            return target.substring(startIndex).trim();
        }
        return "startIndex=" + startIndex + ", endIndex=" + endIndex;
    }

    private static String extractSpecifiedChapterContent(PDDocument document, PDOutlineItem item, String targetTitle, PDOutlineItem uncleItem) throws IOException {
        while (item != null) {
            String title = item.getTitle();
            PDOutlineItem nextItem = item.getNextSibling();

            if (title.equals(targetTitle)) {
                int startPage = getPageNumber(item, document);
                int endPage = document.getNumberOfPages(); // 默认到文档末尾


                if (nextItem != null) {
                    endPage = getPageNumber(nextItem, document);
                    endString = nextItem.getTitle();
                    return extractTextFromPages(document, startPage, endPage);
                }
                if (uncleItem != null) {
                    endPage = getPageNumber(uncleItem, document);
                    endString = uncleItem.getTitle();
                    return extractTextFromPages(document, startPage, endPage);
                }else {
                    endString = "\0";
                    return extractTextFromPages(document, startPage, endPage);
                }
            }

            if (item.getFirstChild() != null) {
                String foundInChild;
                if(nextItem != null) {
                    foundInChild = extractSpecifiedChapterContent(document, item.getFirstChild(), targetTitle, nextItem);
                }
                else {
                    foundInChild = extractSpecifiedChapterContent(document, item.getFirstChild(), targetTitle, uncleItem);
                }
                if (foundInChild != null) {
                    return foundInChild; // 在子项中找到目标章节后返回
                }
            }
            item = item.getNextSibling();
        }
        return null;
    }

    private static int getPageNumber(PDOutlineItem item, PDDocument document) throws IOException {
        PDDestination destination = item.getDestination();
        if (destination == null && item.getAction() instanceof PDActionGoTo) {
            destination = ((PDActionGoTo) item.getAction()).getDestination();
        }

        if (destination instanceof PDPageDestination) {
            PDPageDestination pageDestination = (PDPageDestination) destination;
            PDPage page = pageDestination.getPage();
            if (page == null) {
                int pageIndex = pageDestination.getPageNumber();
                if (pageIndex >= 0 && pageIndex < document.getNumberOfPages()) {
                    page = document.getPage(pageIndex);
                }
            }

            if (page != null) {
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    if (document.getPage(i).equals(page)) {
                        return i + 1; // 返回1-based页面索引
                    }
                }
            }
        }
        return -1; // 无效页面号
    }

    private static String extractTextFromPages(PDDocument document, int startPage, int endPage) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        pdfStripper.setStartPage(startPage);
        pdfStripper.setEndPage(endPage);
        return pdfStripper.getText(document);
    }
}
