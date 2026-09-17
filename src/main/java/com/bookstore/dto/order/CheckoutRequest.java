package com.bookstore.dto.order;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(
        @NotBlank(message = "Shipping address is required")
        String shippingAddress,

        @NotBlank(message = "Contact phone is required")
        String contactPhone
) {}
