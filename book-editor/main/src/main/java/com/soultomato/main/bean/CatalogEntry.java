package com.soultomato.main.bean;

import com.google.gson.Gson;
import com.soultomato.main.utils.getBookContent;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogEntry {
    private String title;
    private List<CatalogEntry> subEntries;

    // 构造函数
    public CatalogEntry(String title) {
        this.title = title;
        this.subEntries = new ArrayList<>();
    }

    // 添加子条目
    public void addSubEntry(CatalogEntry entry) {
        subEntries.add(entry);
    }

    // 获取标题
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // 获取子条目列表
    public List<CatalogEntry> getSubEntries() {
        return subEntries;
    }

    // 打印目录结构，递归方法
    public void print(int level) {
        // 创建适当数量的缩进
        String indent = " ".repeat(level * 4);
        System.out.println(indent + title);
        for (CatalogEntry entry : subEntries) {
            entry.print(level + 1);  // 递归打印每个子条目
        }
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void createFileSystem(String basePath, String title, String filename) throws IOException {
        // 创建当前节点的路径
        File file = new File(basePath, title);

        // 判断当前节点是否有子节点
        if (this.subEntries.isEmpty()) {
            // 尝试创建文件，如果已存在则覆盖内容
            try (FileWriter writer = new FileWriter(file, false)) { // false 表示不追加内容，直接覆盖
                String content = getBookContent.extractChapterContent(filename, this.title);

                if (content != null) {
                    String result = getBookContent.reeditorText(content);
                    String[] lines = result.split("\n");

                    // 使用 Stream API 将每一行包围在 <p> 标签中
                    result = Arrays.stream(lines)
                            .map(line -> "<p>" + line + "</p>")
                            .collect(Collectors.joining("\n"));
                    writer.write(result);
                }
            } catch (IOException e) {
                System.out.println("无法写入文件: " + file.getPath());
                throw e; // 再次抛出异常，通知上层有错误发生
            }
        } else {
            // 有子节点，尝试创建文件夹
            if (file.exists() || file.mkdir()) {
                // 递归创建子节点的文件系统结构
                for (CatalogEntry entry : subEntries) {
                    entry.createFileSystem(file.getAbsolutePath(),entry.getTitle(), filename);
                }
            } else {
                System.out.println("文件夹已存在或无法创建: " + file.getPath());
            }
        }
    }

    public boolean updateFileContent(String basePath,String filename, String chapterTitle, String newContent) throws IOException {
        File file = new File(basePath, filename);

        if (this.subEntries.isEmpty() && this.title.equals(chapterTitle)) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(newContent);
                System.out.println("文件内容已更新: " + file.getPath());
                return true;
            } catch (IOException e) {
                System.out.println("无法写入文件: " + file.getPath());
                throw e;
            }
        } else if (file.isDirectory()) {
            for (CatalogEntry entry : subEntries) {
                if (entry.updateFileContent(file.getAbsolutePath(), entry.getTitle() ,chapterTitle, newContent)) {
                    return true;
                }
            }
        }
        return false;
    }

}
