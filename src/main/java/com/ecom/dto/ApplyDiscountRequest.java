package com.ecom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplyDiscountRequest {
    @NotBlank
    private String discountCode;
}
