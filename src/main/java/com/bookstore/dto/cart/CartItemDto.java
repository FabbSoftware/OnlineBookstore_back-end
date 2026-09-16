package com.bookstore.dto.cart;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemDto(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookAuthor,
        BigDecimal bookPrice,
        String bookCoverImageUrl,
        Integer quantity,
        BigDecimal subtotal
) {}
