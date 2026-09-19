package com.job.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.job.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

    // per-upload-call overrides read by ApiUtils.setTimeouts (cloudinary-http44); milliseconds.
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadImage(MultipartFile file) {
        log.info("Uploading image to Cloudinary: {}", file.getOriginalFilename());
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "profile-pictures");
            options.put("allowed_formats", Arrays.asList("jpg", "png", "gif", "webp"));
            options.put("connect_timeout", CONNECT_TIMEOUT_MS);
            options.put("timeout", READ_TIMEOUT_MS);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
            String url = (String) result.get("secure_url");
            log.info("Image uploaded successfully to Cloudinary");
            return url;
        } catch (IOException e) {
            log.error("Image upload to Cloudinary failed", e);
            throw new FileStorageException("Cloudinary image upload failed", e);
        }
    }

    public String uploadResume(MultipartFile file) {
        log.info("Uploading resume to Cloudinary: {}", file.getOriginalFilename());
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "resumes");
            options.put("resource_type", "raw");
            options.put("access_mode", "public");
            options.put("use_filename", true);
            options.put("unique_filename", true);
            options.put("connect_timeout", CONNECT_TIMEOUT_MS);
            options.put("timeout", READ_TIMEOUT_MS);

            Map<String, Object> result = cloudinary.uploader().upload(
                file.getBytes(), options
            );

            String url = (String) result.get("secure_url");
            log.info("Resume uploaded successfully: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Failed to upload resume to Cloudinary", e);
            throw new FileStorageException("Cloudinary resume upload failed", e);
        }
    }

    public void deleteFile(String publicId) {
        log.info("Deleting file from Cloudinary with public id: {}", publicId);
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("File deletion from Cloudinary failed for public id {}: {}", publicId, e.getMessage());
        }
    }
}
