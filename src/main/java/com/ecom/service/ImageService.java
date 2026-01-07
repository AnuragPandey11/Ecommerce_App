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
