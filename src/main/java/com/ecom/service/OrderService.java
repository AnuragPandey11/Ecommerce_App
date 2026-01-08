package com.ecom.service;

import com.ecom.dto.OrderRequest;
import com.ecom.dto.OrderResponse;
import com.ecom.security.UserPrincipal;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(UserPrincipal currentUser, OrderRequest orderRequest);
    List<OrderResponse> getOrdersForUser(UserPrincipal currentUser);
    OrderResponse getOrder(Long orderId, UserPrincipal currentUser);
}
