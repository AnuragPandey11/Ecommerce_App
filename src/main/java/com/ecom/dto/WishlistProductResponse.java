package com.ecom.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WishlistProductResponse {
    private Long id;
    private String name;
    private String slug;
    private BigDecimal priceAfter;
    private String imageUrl;
}
