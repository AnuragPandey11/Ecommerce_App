package com.ecom.dto;

import com.ecom.entity.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DiscountResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDateTime expiryDate;
    private Boolean isActive;
    private Integer maxUsage;
    private Integer usageCount;
    private LocalDateTime createdAt;
}
