package com.job.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {

    @GetMapping("/resume/{filename}")
    public ResponseEntity<Resource> getResume(@PathVariable String filename) throws IOException {
        return serveFile("uploads/resumes/" + filename, "application/pdf");
    }

    @GetMapping("/profile-picture/{filename}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get("uploads/profile-pictures/" + filename);
        String mimeType = Files.probeContentType(filePath);  // Detects png/jpeg/webp/etc.

        return serveFile(
                filePath.toString(),
                mimeType != null ? mimeType : MediaType.APPLICATION_OCTET_STREAM_VALUE
        );
    }

    private ResponseEntity<Resource> serveFile(String path, String contentType) throws IOException {
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName().toString() + "\"")
                .body(resource);
    }
}

