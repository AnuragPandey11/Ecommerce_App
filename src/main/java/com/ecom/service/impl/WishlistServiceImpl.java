package com.ecom.service.impl;

import com.ecom.dto.WishlistProductResponse;
import com.ecom.dto.WishlistRequest;
import com.ecom.dto.WishlistResponse;
import com.ecom.entity.Product;
import com.ecom.entity.User;
import com.ecom.entity.Wishlist;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.repository.WishlistRepository;
import com.ecom.security.UserPrincipal;
import com.ecom.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> createWishlist(user));
        return mapWishlistToWishlistResponse(wishlist);
    }

    @Override
    @Transactional
    public WishlistResponse addProductToWishlist(WishlistRequest wishlistRequest, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
        Product product = productRepository.findById(wishlistRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + wishlistRequest.getProductId()));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> createWishlist(user));

        wishlist.addProduct(product);
        wishlistRepository.save(wishlist);

        return mapWishlistToWishlistResponse(wishlist);
    }

    @Override
    @Transactional
    public void removeProductFromWishlist(Long productId, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found for user"));

        wishlist.removeProduct(product);
        wishlistRepository.save(wishlist);
    }

    private Wishlist createWishlist(User user) {
        Wishlist newWishlist = Wishlist.builder().user(user).build();
        return wishlistRepository.save(newWishlist);
    }

    private WishlistResponse mapWishlistToWishlistResponse(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .products(wishlist.getProducts().stream()
                        .map(this::mapProductToWishlistProductResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private WishlistProductResponse mapProductToWishlistProductResponse(Product product) {
        String imageUrl = product.getImages() != null && !product.getImages().isEmpty()
                ? product.getImages().get(0).getImageUrl()
                : null;

        return WishlistProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .priceAfter(product.getPriceAfter())
                .imageUrl(imageUrl)
                .build();
    }
}
