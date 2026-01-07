package com.ecom.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;
    
    @NotNull(message = "Price before discount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal priceBefore;
    
    @NotNull(message = "Price after discount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal priceAfter;
    
    @NotNull(message = "Inventory is required")
    @Min(value = 0, message = "Inventory cannot be negative")
    private Integer inventory;
    
    private String descriptionHtml;
    
    private Set<Long> categoryIds;
}
