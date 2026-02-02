package com.ecom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2StorageService {

    private final S3Client s3Client;

    @Value("${app.r2.bucket-name}")
    private String bucketName;

    @Value("${app.r2.public-url}")
    private String publicUrl;
    
    // Inject CDN URL, defaulting to empty string if not set (safe for local dev)
    @Value("${app.r2.cdn-url:}")
    private String cdnUrl;

    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String fileName = UUID.randomUUID().toString() + extension;

        try {
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            // LOGIC CHANGE: Return the CDN URL if it is configured; otherwise fallback to R2 Public URL
            String baseUrl = (cdnUrl != null && !cdnUrl.isEmpty()) ? cdnUrl : publicUrl;
            
            return baseUrl + "/" + fileName;

        } catch (IOException e) {
            log.error("Error uploading file to R2", e);
            throw new RuntimeException("Failed to upload file to R2", e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null) {
            return;
        }
        
        String fileName = null;

        // LOGIC CHANGE: Extract filename from either Public URL OR CDN URL
        if (fileUrl.startsWith(publicUrl)) {
            fileName = fileUrl.substring(publicUrl.length() + 1);
        } else if (cdnUrl != null && !cdnUrl.isEmpty() && fileUrl.startsWith(cdnUrl)) {
            fileName = fileUrl.substring(cdnUrl.length() + 1);
        }
        
        // If the URL didn't match either of our known domains, log warning and skip
        if (fileName == null) {
            log.warn("Attempted to delete file with unknown URL prefix: {}", fileUrl);
            return;
        }
        
        // LOGIC CHANGE: Strip query parameters (e.g. ?v=123) if they exist
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileName).build());
            log.info("Deleted file from R2: {}", fileName);
        } catch (Exception e) {
            log.error("Error deleting file from R2: {}", fileName, e);
            // We usually don't throw exception here to prevent blocking the main delete flow
        }
    }
}