package com.ecom.controller;

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

    // Helper method to safely get UserPrincipal
    private UserPrincipal getPrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new RuntimeException("User not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.getWishlist(getPrincipal(authentication)));
    }

    @PostMapping
    public ResponseEntity<WishlistResponse> addProductToWishlist(@Valid @RequestBody WishlistRequest wishlistRequest,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(wishlistService.addProductToWishlist(wishlistRequest, getPrincipal(authentication)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeProductFromWishlist(@PathVariable Long productId,
                                                          Authentication authentication) {
        wishlistService.removeProductFromWishlist(productId, getPrincipal(authentication));
        return ResponseEntity.noContent().build();
    }
}