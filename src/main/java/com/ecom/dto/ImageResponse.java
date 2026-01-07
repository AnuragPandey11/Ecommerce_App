package com.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private Long id;
    private String url;
    private String altText;
    private Integer displayOrder;
    private Boolean isPrimary;
    private String entityType; // PRODUCT, CATEGORY
    private Long entityId;
    private Long fileSize; // in bytes
    private String contentType;
    private LocalDateTime uploadedAt;
}
