package com.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFiltersResponse {
    private List<CategoryResponse> categories;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
