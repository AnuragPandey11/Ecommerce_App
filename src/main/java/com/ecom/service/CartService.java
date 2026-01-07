package com.ecom.service;


import com.ecom.dto.CartItemRequest;
import com.ecom.dto.CartResponse;

public interface CartService {
    CartResponse addToCart(Long userId, CartItemRequest request);
    CartResponse getCart(Long userId);
}
