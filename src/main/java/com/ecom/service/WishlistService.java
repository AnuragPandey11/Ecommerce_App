package com.ecom.service;

import com.ecom.dto.WishlistRequest;
import com.ecom.dto.WishlistResponse;
import com.ecom.security.UserPrincipal;

public interface WishlistService {
    WishlistResponse getWishlist(UserPrincipal currentUser);
    WishlistResponse addProductToWishlist(WishlistRequest wishlistRequest, UserPrincipal currentUser);
    void removeProductFromWishlist(Long productId, UserPrincipal currentUser);
}
