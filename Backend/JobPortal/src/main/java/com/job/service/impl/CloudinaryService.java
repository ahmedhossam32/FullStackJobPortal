package com.job.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.job.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

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
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "profile-pictures"
            ));
            String url = (String) result.get("secure_url");
            log.info("Image uploaded successfully to Cloudinary");
            return url;
        } catch (IOException e) {
            log.error("Image upload to Cloudinary failed: {}", e.getMessage());
            throw new BadRequestException("Image upload failed", e);
        }
    }

    public String uploadResume(MultipartFile file) {
        log.info("Uploading resume to Cloudinary: {}", file.getOriginalFilename());
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "raw");
            options.put("folder", "resumes");
            options.put("format", "pdf");
            options.put("flags", "attachment:false");
            options.put("type", "upload");
            options.put("access_mode", "public");
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
            String url = (String) result.get("secure_url");
            log.info("Resume uploaded successfully to Cloudinary");
            return url;
        } catch (IOException e) {
            log.error("Resume upload to Cloudinary failed: {}", e.getMessage());
            throw new BadRequestException("Resume upload failed", e);
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
