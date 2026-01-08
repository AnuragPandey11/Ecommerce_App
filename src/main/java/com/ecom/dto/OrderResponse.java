package com.ecom.dto;

import com.ecom.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private List<OrderItemResponse> orderItems;
    private BigDecimal subtotal;
    private DiscountResponse discount;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String shippingAddress;
    private LocalDateTime createdAt;
}
