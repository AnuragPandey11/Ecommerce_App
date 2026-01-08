package com.ecom.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WishlistResponse {
    private Long id;
    private List<WishlistProductResponse> products;
}
