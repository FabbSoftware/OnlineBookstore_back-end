package com.bookstore.dto.book;

import java.math.BigDecimal;
import java.util.UUID;

public record BookDto(
        UUID id,
        String title,
        String author,
        BigDecimal price,
        String description,
        String isbn,
        String coverImageUrl,
        Integer stockQuantity
) {}
