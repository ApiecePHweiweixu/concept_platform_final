package com.example.concept_platform.controller;

import com.example.concept_platform.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    private static final String UPLOAD_DIR = "uploads/";
    // Change this to your actual domain/port if needed, or get dynamically
    private static final String BASE_URL = "http://localhost:8080/files/";

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("File is empty");
        }

        // Create directory if not exists
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate new filename
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + suffix;

        // Save file
        File dest = new File(dir.getAbsolutePath() + File.separator + newFilename);
        try {
            file.transferTo(dest);
            String url = BASE_URL + newFilename;
            return Result.success(url);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("Upload failed: " + e.getMessage());
        }
    }
}

