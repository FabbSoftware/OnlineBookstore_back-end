package com.bookstore.controller;

import com.bookstore.domain.User;
import com.bookstore.dto.order.CheckoutRequest;
import com.bookstore.dto.order.OrderDto;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> checkout(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckoutRequest request
    ) {
        User user = getUser(principal);
        OrderDto order = orderService.createOrder(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrderHistory(@AuthenticationPrincipal UserPrincipal principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(orderService.getOrderHistory(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        User user = getUser(principal);
        return ResponseEntity.ok(orderService.getOrderById(user, id));
    }

    private User getUser(UserPrincipal principal) {
        if (principal == null) {
            throw new BadCredentialsException("User is not authenticated");
        }
        return userService.getById(principal.getId());
    }
}
