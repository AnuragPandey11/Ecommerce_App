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
