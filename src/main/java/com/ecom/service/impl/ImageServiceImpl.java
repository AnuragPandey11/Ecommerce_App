package com.ecom.service.impl;


import com.ecom.dto.ImageResponse;
import com.ecom.entity.Image;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.repository.ImageRepository;
import com.ecom.service.ImageService;
import com.ecom.util.FileUploadUtil;
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
    private final FileUploadUtil fileUploadUtil;

    @Override
    public List<ImageResponse> uploadImages(MultipartFile[] files, List<String> altTexts) {
        List<ImageResponse> uploadedImages = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String altText = (altTexts != null && i < altTexts.size()) ? altTexts.get(i) : null;

            // Upload file
            String fileUrl = fileUploadUtil.uploadFile(file, "products");

            // Create image entity
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
        String fileUrl = fileUploadUtil.uploadFile(file, "products");

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

        // Delete file from storage
        fileUploadUtil.deleteFile(image.getUrl());

        // Delete from database
        imageRepository.delete(image);
        log.info("Image deleted successfully: {}", imageId);
    }

    @Override
    public ImageResponse setPrimaryImage(Long productId, Long imageId) {
        // Clear existing primary image
        imageRepository.clearPrimaryImages(Image.ImageEntityType.PRODUCT, productId);

        // Set new primary image
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        image.setIsPrimary(true);
        Image updated = imageRepository.save(image);

        return mapToResponse(updated);
    }

    @Override
    public List<ImageResponse> reorderImages(Long productId, List<Long> imageIds) {
        List<Image> images = new ArrayList<>();

        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

            image.setDisplayOrder(i + 1);
            images.add(imageRepository.save(image));
        }

        return images.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getProductImages(Long productId) {
        List<Image> images = imageRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(
                Image.ImageEntityType.PRODUCT, productId);

        return images.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProductImages(Long productId) {
        List<Image> images = imageRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(
                Image.ImageEntityType.PRODUCT, productId);

        images.forEach(image -> fileUploadUtil.deleteFile(image.getUrl()));
        imageRepository.deleteByEntityTypeAndEntityId(Image.ImageEntityType.PRODUCT, productId);
    }

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
