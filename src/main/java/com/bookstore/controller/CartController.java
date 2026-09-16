package com.bookstore.controller;

import com.bookstore.domain.User;
import com.bookstore.dto.cart.AddToCartRequest;
import com.bookstore.dto.cart.CartDto;
import com.bookstore.dto.cart.UpdateCartItemRequest;
import com.bookstore.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal User user) {
        return null;
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request
    ) {
        return null;
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return null;
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeItemFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable UUID itemId
    ) {
        return null;
    }

    @DeleteMapping
    public ResponseEntity<CartDto> clearCart(@AuthenticationPrincipal User user) {
        return null;
    }
}
