package com.ecom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecom.dto.ApiResponse;
import com.ecom.dto.CartItemRequest;
import com.ecom.dto.CartResponse;
import com.ecom.security.UserPrincipal;
import com.ecom.service.CartService;
import com.ecom.dto.ApplyDiscountRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Slf4j
public class CartController {

    private final CartService cartService;

    // Helper method to safely get User ID
    private Long getUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new RuntimeException("User not authenticated");
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            Authentication authentication, // ✅ Use Authentication instead of @CurrentUser
            @Valid @RequestBody CartItemRequest request
    ) {
        Long userId = getUserId(authentication);
        CartResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            Authentication authentication // ✅ Use Authentication instead of @CurrentUser
    ) {
        Long userId = getUserId(authentication);
        try {
            CartResponse response = cartService.getCart(userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error fetching cart for user: {}", userId, e);
            // Return empty cart structure if none exists/error to prevent frontend crash
            return ResponseEntity.ok(ApiResponse.success(new CartResponse())); 
        }
    }

    @PostMapping("/apply-discount")
    public ResponseEntity<ApiResponse<CartResponse>> applyDiscount(
            Authentication authentication, // ✅ Use Authentication instead of @CurrentUser
            @Valid @RequestBody ApplyDiscountRequest request
    ) {
        Long userId = getUserId(authentication);
        CartResponse response = cartService.applyDiscount(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Discount applied successfully", response));
    }
}