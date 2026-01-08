package com.ecom.controller;

import com.ecom.dto.WishlistRequest;
import com.ecom.dto.WishlistResponse;
import com.ecom.security.CurrentUser;
import com.ecom.security.UserPrincipal;
import com.ecom.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(wishlistService.getWishlist(currentUser));
    }

    @PostMapping
    public ResponseEntity<WishlistResponse> addProductToWishlist(@Valid @RequestBody WishlistRequest wishlistRequest,
                                                                 @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(wishlistService.addProductToWishlist(wishlistRequest, currentUser));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeProductFromWishlist(@PathVariable Long productId,
                                                        @CurrentUser UserPrincipal currentUser) {
        wishlistService.removeProductFromWishlist(productId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
