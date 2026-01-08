package com.ecom.service.impl;


import com.ecom.dto.CartItemRequest;
import com.ecom.dto.*;
import com.ecom.entity.*;
import com.ecom.exception.BadRequestException;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.dto.DiscountResponse;
import com.ecom.entity.Discount;
import com.ecom.repository.*;
import com.ecom.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final DiscountRepository discountRepository;

    @Override
    public CartResponse addToCart(Long userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User", "id", userId)
                );

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product", "id", request.getProductId())
                );

        if (product.getInventory() < request.getQuantity()) {
            throw new BadRequestException("Insufficient inventory for product");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(
                        CartItem.builder()
                                .cart(cart)
                                .product(product)
                                .quantity(0)
                                .build()
                );

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItemRepository.save(cartItem);

        return mapToCartResponse(cart);
    }

    @Transactional(readOnly = true)
    @Override
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse applyDiscount(Long userId, ApplyDiscountRequest applyDiscountRequest) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        Discount discount = discountRepository.findByCode(applyDiscountRequest.getDiscountCode())
                .orElseThrow(() -> new BadRequestException("Invalid discount code"));

        if (!discount.getIsActive() || (discount.getExpiryDate() != null && discount.getExpiryDate().isBefore(LocalDateTime.now()))) {
            throw new BadRequestException("Discount code is not active or has expired");
        }

        if (discount.getMaxUsage() != null && discount.getUsageCount() >= discount.getMaxUsage()) {
            throw new BadRequestException("Discount has reached its maximum usage limit");
        }

        cart.setDiscount(discount);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();
                    String primaryImage = product.getImages().stream()
                            .filter(ProductImage::getIsPrimary)
                            .findFirst()
                            .map(ProductImage::getImageUrl)
                            .orElse(null);

                    BigDecimal price = product.getPriceAfter();
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

                    return CartItemResponse.builder()
                            .id(item.getId())
                            .productId(product.getId())
                            .productName(product.getName())
                            .productSlug(product.getSlug())
                            .price(price)
                            .quantity(item.getQuantity())
                            .subtotal(subtotal)
                            .primaryImageUrl(primaryImage)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal subtotalAmount = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = subtotalAmount;
        DiscountResponse discountResponse = null;

        if (cart.getDiscount() != null) {
            Discount discount = cart.getDiscount();
            if (discount.getIsActive() && (discount.getExpiryDate() == null || discount.getExpiryDate().isAfter(LocalDateTime.now()))) {
                if (discount.getDiscountType() == com.ecom.entity.DiscountType.PERCENTAGE) {
                    BigDecimal discountValue = subtotalAmount.multiply(discount.getDiscountValue().divide(new BigDecimal(100)));
                    totalAmount = subtotalAmount.subtract(discountValue);
                } else if (discount.getDiscountType() == com.ecom.entity.DiscountType.FIXED_AMOUNT) {
                    totalAmount = subtotalAmount.subtract(discount.getDiscountValue());
                }
                discountResponse = DiscountResponse.builder()
                        .id(discount.getId())
                        .code(discount.getCode())
                        .discountType(discount.getDiscountType())
                        .discountValue(discount.getDiscountValue())
                        .build();
            }
        }
        
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .subtotal(subtotalAmount)
                .discount(discountResponse)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }
}

