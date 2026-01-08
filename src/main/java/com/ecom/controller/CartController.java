package com.ecom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.dto.ApiResponse;
import com.ecom.dto.CartItemRequest;
import com.ecom.dto.CartResponse;
import com.ecom.security.CurrentUser;
import com.ecom.security.UserPrincipal;
import com.ecom.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

   @PostMapping("/add")
public ResponseEntity<ApiResponse<CartResponse>> addToCart(
        @CurrentUser UserPrincipal principal,
        @Valid @RequestBody CartItemRequest request
) {
    Long userId = principal.getId();
    CartResponse response = cartService.addToCart(userId, request);
    return ResponseEntity.ok(ApiResponse.success("Item added to cart", response));
}


    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @CurrentUser UserPrincipal principal
    ) {
        Long userId = principal.getId();
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/apply-discount")
    public ResponseEntity<ApiResponse<CartResponse>> applyDiscount(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody com.ecom.dto.ApplyDiscountRequest request
    ) {
        Long userId = principal.getId();
        CartResponse response = cartService.applyDiscount(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Discount applied successfully", response));
    }
}

