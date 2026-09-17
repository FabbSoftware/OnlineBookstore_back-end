package com.bookstore.dto.order;

import com.bookstore.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        List<OrderItemDto> items,
        BigDecimal totalAmount,
        OrderStatus status,
        String shippingAddress,
        String contactPhone,
        Instant createdAt
) {}
