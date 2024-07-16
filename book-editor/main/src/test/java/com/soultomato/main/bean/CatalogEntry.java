package com.soultomato.main.bean;

import java.util.ArrayList;
import java.util.List;

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

    // 获取子条目列表
    public List<CatalogEntry> getSubEntries() {
        return subEntries;
    }

    // 打印目录结构，递归方法
    public void print(int level) {
        // 创建适当数量的缩进
        String indent = "-".repeat(level * 4);
        System.out.println(indent + title);
        for (CatalogEntry entry : subEntries) {
            entry.print(level + 1);  // 递归打印每个子条目
        }
    }
}
