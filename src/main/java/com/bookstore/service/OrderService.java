package com.bookstore.service;

import com.bookstore.domain.*;
import com.bookstore.dto.order.CheckoutRequest;
import com.bookstore.dto.order.OrderDto;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, CartService cartService, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.orderMapper = orderMapper;
    }

    public OrderDto createOrder(User user, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCartEntity(user);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart");
        }

        for (CartItem cartItem : cart.getItems()) {
            Book book = cartItem.getBook();
            if (cartItem.getQuantity() > book.getStockQuantity()) {
                throw new BadRequestException("Not enough stock for book: " + book.getTitle());
            }
        }

        Order order = getOrder(user, request, cart);
        Order savedOrder = orderRepository.save(order);

        cart.clear();

        return orderMapper.toDto(savedOrder);
    }

    private static Order getOrder(User user, CheckoutRequest request, Cart cart) {
        Order order = new Order(user, BigDecimal.ZERO, OrderStatus.CONFIRMED, request.shippingAddress(), request.contactPhone());
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Book book = cartItem.getBook();
            book.setStockQuantity(book.getStockQuantity() - cartItem.getQuantity());

            OrderItem orderItem = new OrderItem(order, book, cartItem.getQuantity(), book.getPrice());
            order.addItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        return order;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrderHistory(User user) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return orderMapper.toDtoList(orders);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(User user, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        return orderMapper.toDto(order);
    }
}
