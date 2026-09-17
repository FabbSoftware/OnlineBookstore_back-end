package com.bookstore.service;

import com.bookstore.domain.*;
import com.bookstore.dto.order.CheckoutRequest;
import com.bookstore.dto.order.OrderDto;
import com.bookstore.dto.order.OrderItemDto;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Book book1;
    private Book book2;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User(UUID.randomUUID(), "buyer@example.com", "secret", "Buyer Name", Role.ROLE_USER);
        book1 = new Book(UUID.randomUUID(), "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "Clean code desc", "978-0132350884", "cover1.jpg", 10);
        book2 = new Book(UUID.randomUUID(), "The Pragmatic Programmer", "David Thomas", new BigDecimal("39.99"), "Pragmatic desc", "978-0135957059", "cover2.jpg", 5);

        cart = new Cart(UUID.randomUUID(), user, new ArrayList<>());
        cart.getItems().add(new CartItem(UUID.randomUUID(), cart, book1, 2));
        cart.getItems().add(new CartItem(UUID.randomUUID(), cart, book2, 1));
    }

    @Test
    void shouldCreateOrderSuccessfullyFromCart() {
        CheckoutRequest checkoutRequest = new CheckoutRequest("123 Main St, New York, NY", "+1-555-0199");

        when(cartService.getOrCreateCartEntity(user)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID orderId = UUID.randomUUID();
        OrderItemDto item1 = new OrderItemDto(UUID.randomUUID(), book1.getId(), book1.getTitle(), book1.getAuthor(), book1.getCoverImageUrl(), book1.getPrice(), 2, new BigDecimal("69.98"));
        OrderItemDto item2 = new OrderItemDto(UUID.randomUUID(), book2.getId(), book2.getTitle(), book2.getAuthor(), book2.getCoverImageUrl(), book2.getPrice(), 1, new BigDecimal("39.99"));
        OrderDto expectedDto = new OrderDto(orderId, List.of(item1, item2), new BigDecimal("109.97"), OrderStatus.CONFIRMED, "123 Main St, New York, NY", "+1-555-0199", Instant.now());
        when(orderMapper.toDto(any(Order.class))).thenReturn(expectedDto);

        OrderDto result = orderService.createOrder(user, checkoutRequest);

        assertThat(result).isNotNull();
        assertThat(result.totalAmount()).isEqualByComparingTo("109.97");
        assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMED);

        // Verify stock deducted
        assertThat(book1.getStockQuantity()).isEqualTo(8); // 10 - 2
        assertThat(book2.getStockQuantity()).isEqualTo(4); // 5 - 1

        // Verify cart is cleared
        assertThat(cart.getItems()).isEmpty();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenCartIsEmptyOnCheckout() {
        cart.clear();
        when(cartService.getOrCreateCartEntity(user)).thenReturn(cart);

        CheckoutRequest request = new CheckoutRequest("123 Main St", "555-1234");
        assertThatThrownBy(() -> orderService.createOrder(user, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot checkout with an empty cart");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenItemExceedsStockOnCheckout() {
        book1.setStockQuantity(1); // cart has quantity 2
        when(cartService.getOrCreateCartEntity(user)).thenReturn(cart);

        CheckoutRequest request = new CheckoutRequest("123 Main St", "555-1234");
        assertThatThrownBy(() -> orderService.createOrder(user, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Not enough stock for book");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldReturnOrderHistoryForUser() {
        Order order = new Order(UUID.randomUUID(), user, new ArrayList<>(), new BigDecimal("50.00"), OrderStatus.CONFIRMED, "123 St", "555-0000");
        OrderDto dto = new OrderDto(order.getId(), List.of(), new BigDecimal("50.00"), OrderStatus.CONFIRMED, "123 St", "555-0000", Instant.now());

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of(order));
        when(orderMapper.toDtoList(List.of(order))).thenReturn(List.of(dto));

        List<OrderDto> history = orderService.getOrderHistory(user);

        assertThat(history).hasSize(1);
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Test
    void shouldReturnOrderByIdForUser() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, user, new ArrayList<>(), new BigDecimal("50.00"), OrderStatus.CONFIRMED, "123 St", "555-0000");
        OrderDto dto = new OrderDto(orderId, List.of(), new BigDecimal("50.00"), OrderStatus.CONFIRMED, "123 St", "555-0000", Instant.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = orderService.getOrderById(user, orderId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(orderId);
    }

    @Test
    void shouldThrowNotFoundWhenOrderDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(orderRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(user, unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id:");
    }

    @Test
    void shouldThrowNotFoundWhenOrderBelongsToAnotherUser() {
        UUID orderId = UUID.randomUUID();
        User anotherUser = new User(UUID.randomUUID(), "other@example.com", "pass", "Other", Role.ROLE_USER);
        Order order = new Order(orderId, anotherUser, new ArrayList<>(), new BigDecimal("50.00"), OrderStatus.CONFIRMED, "123 St", "555-0000");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(user, orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id:");
    }
}
