package com.bookstore.service;

import com.bookstore.domain.User;
import com.bookstore.dto.order.CheckoutRequest;
import com.bookstore.dto.order.OrderDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    public OrderDto createOrder(User user, CheckoutRequest request) {
        return null;
    }

    public List<OrderDto> getOrderHistory(User user) {
        return null;
    }

    public OrderDto getOrderById(User user, UUID orderId) {
        return null;
    }
}
