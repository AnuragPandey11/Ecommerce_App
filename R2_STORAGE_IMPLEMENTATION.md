# R2 Cloudflare Storage Implementation

This document details the implementation of Cloudflare R2 storage for image uploads in the E-commerce application.

## Overview

The system is configured to upload images directly to a Cloudflare R2 bucket (S3-compatible).

1.  **Upload:** Admin uploads images via `ImageController`.
2.  **Storage:** `R2StorageService` handles the upload to the R2 bucket.
3.  **Persistence:** The public URL of the uploaded image is stored in the `images` database table.
4.  **Retrieval:** The API returns the public URL, allowing the frontend to load images directly from the R2 CDN.

---

## 1. Configuration (`R2Config.java`)

Configures the `S3Client` bean to connect to the Cloudflare R2 endpoint using credentials from `application.properties`.

```java
package com.ecom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${app.r2.access-key}")
    private String accessKey;

    @Value("${app.r2.secret-key}")
    private String secretKey;

    @Value("${app.r2.account-id}")
    private String accountId;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .region(Region.US_EAST_1) // R2 requires a region setting, though it ignores it. US_EAST_1 is standard.
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
```

---

## 2. Storage Service (`R2StorageService.java`)

Handles the low-level S3 operations (upload and delete). It generates a unique filename using UUID and constructs the public URL.

```java
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
            
            return publicUrl + "/" + fileName;
        } catch (IOException e) {
            log.error("Error uploading file to R2", e);
            throw new RuntimeException("Failed to upload file to CDN", e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(publicUrl)) {
            return;
        }
        String fileName = fileUrl.substring(publicUrl.length() + 1);
        
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileName).build());
        log.info("Deleted file from R2: {}", fileName);
    }
}
```

---

## 3. Image Entity (`Image.java`)

Represents the image metadata stored in the database, including the `url` which points to R2.

```java
package com.ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "entity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_name")
    private String fileName;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    public enum ImageEntityType {
        PRODUCT,
        CATEGORY,
        USER,
        BRAND
    }
}
```

---

## 4. Image Service (`ImageService.java` & `ImageServiceImpl.java`)

The service layer orchestrates the upload process. It calls `R2StorageService` to upload the physical file and then saves the returned URL and metadata to the database.

### Interface
```java
package com.ecom.service;

import com.ecom.dto.ImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<ImageResponse> uploadImages(MultipartFile[] files, List<String> altTexts);
    ImageResponse uploadSingleImage(MultipartFile file, String altText);
    void deleteImage(Long imageId);
    ImageResponse setPrimaryImage(Long productId, Long imageId);
    List<ImageResponse> reorderImages(Long productId, List<Long> imageIds);
    List<ImageResponse> getProductImages(Long productId);
    void deleteProductImages(Long productId);
}
```

### Implementation
```java
package com.ecom.service.impl;

import com.ecom.dto.ImageResponse;
import com.ecom.entity.Image;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.repository.ImageRepository;
import com.ecom.service.ImageService;
import com.ecom.service.R2StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final R2StorageService r2StorageService;

    @Override
    public List<ImageResponse> uploadImages(MultipartFile[] files, List<String> altTexts) {
        List<ImageResponse> uploadedImages = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String altText = (altTexts != null && i < altTexts.size()) ? altTexts.get(i) : null;

            // Upload file to R2
            String fileUrl = r2StorageService.uploadFile(file);

            // Create image entity with R2 URL
            Image image = Image.builder()
                    .url(fileUrl)
                    .altText(altText)
                    .displayOrder(i + 1)
                    .isPrimary(i == 0) // First image is primary by default
                    .entityType(Image.ImageEntityType.PRODUCT)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .fileName(file.getOriginalFilename())
                    .build();

            Image saved = imageRepository.save(image);
            uploadedImages.add(mapToResponse(saved));
        }

        return uploadedImages;
    }

    @Override
    public ImageResponse uploadSingleImage(MultipartFile file, String altText) {
        String fileUrl = r2StorageService.uploadFile(file);

        Image image = Image.builder()
                .url(fileUrl)
                .altText(altText)
                .displayOrder(1)
                .isPrimary(true)
                .entityType(Image.ImageEntityType.PRODUCT)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .fileName(file.getOriginalFilename())
                .build();

        Image saved = imageRepository.save(image);
        return mapToResponse(saved);
    }

    @Override
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        // Delete file from R2
        r2StorageService.deleteFile(image.getUrl());

        // Delete from database
        imageRepository.delete(image);
        log.info("Image deleted successfully: {}", imageId);
    }

    // ... (Other methods: setPrimaryImage, reorderImages, getProductImages, deleteProductImages, mapToResponse)

    private ImageResponse mapToResponse(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .url(image.getUrl())
                .altText(image.getAltText())
                .displayOrder(image.getDisplayOrder())
                .isPrimary(image.getIsPrimary())
                .entityType(image.getEntityType().name())
                .entityId(image.getEntityId())
                .fileSize(image.getFileSize())
                .contentType(image.getContentType())
                .uploadedAt(image.getUploadedAt())
                .build();
    }
}
```

---

## 5. Controller (`ImageController.java`)

Exposes the endpoints. When an image is uploaded, it returns the `ImageResponse` containing the R2 URL, which the frontend can use immediately.

```java
package com.ecom.controller;

import com.ecom.dto.ApiResponse;
import com.ecom.dto.ImageResponse;
import com.ecom.service.ImageService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // Upload multiple images for product/category
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ImageResponse>>> uploadImages(
            @NotNull @RequestParam("images") MultipartFile[] files,
            @RequestParam(value = "altTexts", required = false) List<String> altTexts
    ) {
        List<ImageResponse> uploadedImages = imageService.uploadImages(files, altTexts);
        return ResponseEntity.ok(ApiResponse.success("Images uploaded successfully", uploadedImages));
    }

    // Upload single image
    @PostMapping(value = "/upload/single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImageResponse>> uploadSingleImage(
            @NotNull @RequestParam("image") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText
    ) {
        ImageResponse image = imageService.uploadSingleImage(file, altText);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", image));
    }

    // Delete image
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteImage(@PathVariable Long imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully"));
    }

    // Set primary image for product
    @PutMapping("/products/{productId}/primary-image/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImageResponse>> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        ImageResponse primaryImage = imageService.setPrimaryImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success("Primary image set successfully", primaryImage));
    }

    // Reorder images
    @PutMapping("/products/{productId}/images/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ImageResponse>>> reorderImages(
            @PathVariable Long productId,
            @RequestBody List<Long> imageIds
    ) {
        List<ImageResponse> reorderedImages = imageService.reorderImages(productId, imageIds);
        return ResponseEntity.ok(ApiResponse.success("Images reordered successfully", reorderedImages));
    }
}
```