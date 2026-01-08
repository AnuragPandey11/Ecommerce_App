package com.ecom.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private List<CartItemRequest> cartItems;
    private String shippingAddress;
    private Long discountId;
}
