package com.ecom.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.dto.OrderRequest;
import com.ecom.dto.OrderResponse;
import com.ecom.security.CurrentUser;
import com.ecom.security.UserPrincipal;
import com.ecom.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> createOrder(@CurrentUser UserPrincipal currentUser, @RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = orderService.createOrder(currentUser, orderRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getOrdersForUser(@CurrentUser UserPrincipal currentUser) {
        List<OrderResponse> orders = orderService.getOrdersForUser(currentUser);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId, @CurrentUser UserPrincipal currentUser) {
        OrderResponse order = orderService.getOrder(orderId, currentUser);
        return ResponseEntity.ok(order);
    }
}
