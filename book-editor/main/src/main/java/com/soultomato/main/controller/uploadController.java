package com.soultomato.main.controller;

import com.google.gson.Gson;
import com.soultomato.main.bean.CatalogEntry;
import com.soultomato.main.service.uploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
public class uploadController {

    @Autowired
    private uploadService service;


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String json = service.uploadFile(file);
            return ResponseEntity.ok().body(json);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/chapter-content")
    public String getChapterContent(@RequestParam String mainTitle, @RequestParam String chapterTitle) {
        return service.getBookContentByTitle(mainTitle, chapterTitle);
    }

    @PostMapping("/update")
    public boolean updateContent(@RequestBody Map<String, Object> data){
        String html = (String) data.get("html");
        String title= (String) data.get("title");

        return service.updateContentByTitle(title, html);
    }

    @PostMapping("/saveRoot")
    public void saveRoot() {
        System.out.println("saveRoot endpoint called");
        service.saveRootToFile();
    }

    @GetMapping("/getRoot")
    public String init() {
        String json = service.loadRootFromFile();
        return json;
    }
}
