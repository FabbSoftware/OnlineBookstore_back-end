package com.bookstore.mapper;

import com.bookstore.domain.Order;
import com.bookstore.domain.OrderItem;
import com.bookstore.dto.order.OrderDto;
import com.bookstore.dto.order.OrderItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    @Mapping(source = "book.author", target = "bookAuthor")
    @Mapping(source = "book.coverImageUrl", target = "bookCoverImageUrl")
    @Mapping(source = "subtotal", target = "subtotal")
    OrderItemDto toItemDto(OrderItem item);

    List<OrderItemDto> toItemDtoList(List<OrderItem> items);

    OrderDto toDto(Order order);

    List<OrderDto> toDtoList(List<Order> orders);
}
