package com.bookstore.controller;

import com.bookstore.domain.User;
import com.bookstore.dto.order.CheckoutRequest;
import com.bookstore.dto.order.OrderDto;
import com.bookstore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> checkout(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CheckoutRequest request
    ) {
        return null;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrderHistory(@AuthenticationPrincipal User user) {
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        return null;
    }
}
