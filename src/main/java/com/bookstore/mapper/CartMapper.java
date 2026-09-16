package com.bookstore.mapper;

import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.dto.cart.CartDto;
import com.bookstore.dto.cart.CartItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    @Mapping(source = "book.author", target = "bookAuthor")
    @Mapping(source = "book.price", target = "bookPrice")
    @Mapping(source = "book.coverImageUrl", target = "bookCoverImageUrl")
    @Mapping(source = "subtotal", target = "subtotal")
    CartItemDto toItemDto(CartItem item);

    List<CartItemDto> toItemDtoList(List<CartItem> items);

    @Mapping(source = "items", target = "items")
    @Mapping(source = "totalItems", target = "totalItems")
    @Mapping(source = "totalAmount", target = "totalAmount")
    CartDto toDto(Cart cart);
}
