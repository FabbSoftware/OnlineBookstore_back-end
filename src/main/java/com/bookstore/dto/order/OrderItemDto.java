package com.bookstore.dto.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookAuthor,
        String bookCoverImageUrl,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal
) {}
