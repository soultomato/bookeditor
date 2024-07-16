package com.soultomato.main.utils;

import com.soultomato.main.bean.CatalogEntry;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.io.File;
import java.io.IOException;

public class getBookmarks {

    public static CatalogEntry getBookmarks(String pdfPath) {
        File file = new File(pdfPath);
        String filename = file.getName();
        CatalogEntry root = new CatalogEntry(filename);

        try (PDDocument document = Loader.loadPDF(file)) {
            if (!document.isEncrypted()) {
                PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
                if (outline != null) {
                    PDOutlineItem bookmark = outline.getFirstChild();

                    while (bookmark != null) {
                        CatalogEntry chapter = new CatalogEntry(bookmark.getTitle().trim());
                        root.addSubEntry(chapter);

                        PDOutlineItem child = bookmark.getFirstChild();
                        if (child != null) {
                            PDOutlineItem cchild = child;

                            while (cchild != null) {
                                CatalogEntry section = new CatalogEntry(cchild.getTitle());
                                chapter.addSubEntry(section);

                                PDOutlineItem grandchild = cchild.getFirstChild();
                                if(grandchild != null){
                                    PDOutlineItem ggrandchild = grandchild;

                                    while (ggrandchild != null) {
                                        CatalogEntry subsection = new CatalogEntry(ggrandchild.getTitle());
                                        section.addSubEntry(subsection);
                                        ggrandchild = ggrandchild.getNextSibling();
                                    }
                                }
                                cchild = cchild.getNextSibling();
                            }
                        }
                        bookmark = bookmark.getNextSibling();
                    }
                } else {
                    System.out.println("This document does not contain any bookmarks.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        root.print(0);

        return root;
    }
}
