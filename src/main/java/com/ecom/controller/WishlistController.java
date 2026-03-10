package com.ecom.controller;

import com.ecom.dto.ApiResponse;
import com.ecom.dto.WishlistRequest;
import com.ecom.dto.WishlistResponse;
import com.ecom.security.UserPrincipal;
import com.ecom.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class WishlistController {

    private final WishlistService wishlistService;

    private UserPrincipal getPrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new RuntimeException("User not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(Authentication authentication) {
        WishlistResponse wishlist = wishlistService.getWishlist(getPrincipal(authentication));
        return ResponseEntity.ok(ApiResponse.success(wishlist));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> addProductToWishlist(
            @Valid @RequestBody WishlistRequest wishlistRequest,
            Authentication authentication) {
        WishlistResponse wishlist = wishlistService.addProductToWishlist(wishlistRequest, getPrincipal(authentication));
        return ResponseEntity.ok(ApiResponse.success("Product added to wishlist", wishlist));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> removeProductFromWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        wishlistService.removeProductFromWishlist(productId, getPrincipal(authentication));
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist", null));
    }
}
