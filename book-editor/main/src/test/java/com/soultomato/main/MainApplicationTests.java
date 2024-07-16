package com.soultomato.main;

import com.soultomato.main.utils.getBookContent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;


@SpringBootTest
class MainApplicationTests {

    @Test
    void contextLoads() throws IOException {

        String pdfFilePath = "遥感图像视觉分析的算法理论和应用.pdf";
        String targetChapterTitle = "序言";

        String out = getBookContent.extractChapterContent(pdfFilePath, targetChapterTitle);

        System.out.println(getBookContent.reeditorText(out));

    }
}
