package com.bookstore.controller;

import com.bookstore.domain.User;
import com.bookstore.dto.cart.AddToCartRequest;
import com.bookstore.dto.cart.CartDto;
import com.bookstore.dto.cart.UpdateCartItemRequest;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.CartService;
import com.bookstore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddToCartRequest request
    ) {
        User user = getUser(principal);
        return ResponseEntity.ok(cartService.addItemToCart(user, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateItemQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        User user = getUser(principal);
        return ResponseEntity.ok(cartService.updateItemQuantity(user, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeItemFromCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID itemId
    ) {
        User user = getUser(principal);
        return ResponseEntity.ok(cartService.removeItemFromCart(user, itemId));
    }

    @DeleteMapping
    public ResponseEntity<CartDto> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(cartService.clearCart(user));
    }

    private User getUser(UserPrincipal principal) {
        if (principal == null) {
            throw new BadCredentialsException("User is not authenticated");
        }
        return userService.getById(principal.getId());
    }
}
