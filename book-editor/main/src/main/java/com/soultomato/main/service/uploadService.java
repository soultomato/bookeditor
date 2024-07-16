package com.soultomato.main.service;

import com.google.gson.Gson;
import com.soultomato.main.bean.CatalogEntry;
import com.soultomato.main.utils.getBookmarks;
import com.soultomato.main.utils.getBookContent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.catalog.Catalog;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class uploadService {
    private final String path = "uploads";
    private final Path fileStorageLocation = Paths.get(path);
    private CatalogEntry root;

    private final String rootFilePath = "root.json";  // 用于保存 root 对象的文件路径

    public uploadService() {
        loadRootFromFile();
    }

    public String uploadFile(MultipartFile file) throws Exception {
        // 检查是否为PDF文件
        if (!"application/pdf".equals(file.getContentType())) {
            throw new RuntimeException("只允许上传PDF文件");
        }

        // 存储文件到服务器
        Path targetLocation = this.fileStorageLocation.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        root = getBookmarks.getBookmarks(targetLocation.toString());

        root.createFileSystem(path, root.getTitle().substring(0, root.getTitle().lastIndexOf(".")),root.getTitle());

        // 可以在这里添加获取书签和其他逻辑
        return root.toJson();
    }

    public String getBookContentByTitle(String filename, String chapterTitle) {
        String content = null;
        try {
            Path filePath = getBookContent.findFileInDirectory(path+"/"+root.getTitle().substring(0, root.getTitle().lastIndexOf(".")), chapterTitle);
            if (filePath != null) {
                content = getBookContent.readFile(filePath);
                if (content != null) {
                    return content;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        content = getBookContent.extractChapterContent(filename, chapterTitle);

        if (content != null) {
            String result = getBookContent.reeditorText(content);
            String[] lines = result.split("\n");

            // 使用 Stream API 将每一行包围在 <p> 标签中
            result = Arrays.stream(lines)
                    .map(line -> "<p>" + line + "</p>")
                    .collect(Collectors.joining("\n"));

            return result;
        }
        else {
            return "读取内容失败";
        }
    }

    public boolean updateContentByTitle(String chapterTitle, String content){

        boolean result = false;
        try {
            result = root.updateFileContent(path, root.getTitle().substring(0, root.getTitle().lastIndexOf(".")) ,chapterTitle, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveRootToFile() {
        if (root != null) {
            try (FileWriter writer = new FileWriter(rootFilePath)) {
                Gson gson = new Gson();
                gson.toJson(root, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("root saved to " + rootFilePath);
    }

    // 从文件加载 root 对象
    public String loadRootFromFile() {
        File file = new File(rootFilePath);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                root = gson.fromJson(reader, CatalogEntry.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (root == null) {
            return null;
        }
        return root.toJson();
    }
}
