package com.ecom.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.dto.DiscountResponse;
import com.ecom.dto.OrderItemResponse;
import com.ecom.dto.OrderRequest;
import com.ecom.dto.OrderResponse;
import com.ecom.entity.Discount;
import com.ecom.entity.DiscountType;
import com.ecom.entity.Order;
import com.ecom.entity.OrderItem;
import com.ecom.entity.OrderStatus;
import com.ecom.entity.Product;
import com.ecom.entity.User;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.repository.DiscountRepository;
import com.ecom.repository.OrderRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.security.UserPrincipal;
import com.ecom.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(UserPrincipal currentUser, OrderRequest orderRequest) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        Order order = Order.builder()
                .user(user)
                .shippingAddress(orderRequest.getShippingAddress())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = orderRequest.getCartItems().stream().<OrderItem>map(cartItemRequest -> {
            Product product = productRepository.findById(cartItemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + cartItemRequest.getProductId()));
            return OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItemRequest.getQuantity())
                    .price(product.getPriceAfter())
                    .build();
        }).toList();

        order.setOrderItems(orderItems);

        BigDecimal subtotal = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);

        BigDecimal totalPrice = subtotal;
        if (orderRequest.getDiscountId() != null) {
            Discount discount = discountRepository.findById(orderRequest.getDiscountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + orderRequest.getDiscountId()));
            order.setDiscount(discount);
            if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
                BigDecimal discountValue = subtotal.multiply(discount.getDiscountValue().divide(BigDecimal.valueOf(100)));
                totalPrice = subtotal.subtract(discountValue);
            } else if (discount.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                totalPrice = subtotal.subtract(discount.getDiscountValue());
            }
        }
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        return mapOrderToOrderResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getOrdersForUser(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
        return orderRepository.findByUser(user).stream()
                .map(this::mapOrderToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrder(Long orderId, UserPrincipal currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Basic authorization check
        if (!order.getUser().getId().equals(currentUser.getId())) {
            // We might want to check for admin role here in the future
            throw new SecurityException("You are not authorized to view this order.");
        }

        return mapOrderToOrderResponse(order);
    }

    private DiscountResponse mapDiscountToDiscountResponse(Discount discount) {
        if (discount == null) {
            return null;
        }
        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .expiryDate(discount.getExpiryDate())
                .isActive(discount.getIsActive())
                .maxUsage(discount.getMaxUsage())
                .usageCount(discount.getUsageCount())
                .createdAt(discount.getCreatedAt())
                .build();
    }
    
    private OrderResponse mapOrderToOrderResponse(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(this::mapOrderItemToOrderItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderItems(orderItemResponses)
                .subtotal(order.getSubtotal())
                .discount(mapDiscountToDiscountResponse(order.getDiscount()))
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemResponse mapOrderItemToOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }
}


