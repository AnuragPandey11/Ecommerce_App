package com.ecom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<CartItemRequest> cartItems;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private Long discountId;
}
