package com.ecom.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest {
    private MultipartFile[] images;
    private List<String> altTexts;
    private Long entityId; // Product ID or Category ID
    private String entityType; // "PRODUCT" or "CATEGORY"
}
