package com.bookstore.controller;

import com.bookstore.domain.OrderStatus;
import com.bookstore.domain.Role;
import com.bookstore.domain.User;
import com.bookstore.dto.order.CheckoutRequest;
import com.bookstore.dto.order.OrderDto;
import com.bookstore.dto.order.OrderItemDto;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.GlobalExceptionHandler;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.security.UserPrincipal;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderController orderController;

    private User testUser;
    private UserPrincipal testPrincipal;
    private UUID orderId;
    private OrderDto sampleOrderDto;

    @BeforeEach
    void setUp() {
        testUser = new User(UUID.randomUUID(), "buyer@example.com", "secret", "Buyer Name", Role.ROLE_USER);
        testPrincipal = UserPrincipal.fromUserForJwt(testUser);
        orderId = UUID.randomUUID();

        lenient().when(userService.getById(any(UUID.class))).thenReturn(testUser);

        OrderItemDto itemDto = new OrderItemDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Clean Code",
                "Robert C. Martin",
                "cover.jpg",
                new BigDecimal("34.99"),
                1,
                new BigDecimal("34.99")
        );

        sampleOrderDto = new OrderDto(
                orderId,
                List.of(itemDto),
                new BigDecimal("34.99"),
                OrderStatus.CONFIRMED,
                "123 Main St",
                "555-1234",
                Instant.now()
        );

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testPrincipal;
            }
        };

        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCheckoutAndReturn201Created() throws Exception {
        CheckoutRequest request = new CheckoutRequest("123 Main St", "555-1234");
        when(orderService.createOrder(eq(testUser), any(CheckoutRequest.class))).thenReturn(sampleOrderDto);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(orderId.toString())))
                .andExpect(jsonPath("$.totalAmount", is(34.99)))
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.shippingAddress", is("123 Main St")));
    }

    @Test
    void shouldReturn400WhenCheckoutPayloadIsInvalid() throws Exception {
        CheckoutRequest invalidRequest = new CheckoutRequest("", "");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void shouldReturn400WhenCheckoutFailsDueToEmptyCart() throws Exception {
        CheckoutRequest request = new CheckoutRequest("123 Main St", "555-1234");
        when(orderService.createOrder(eq(testUser), any(CheckoutRequest.class)))
                .thenThrow(new BadRequestException("Cannot checkout with an empty cart"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Cannot checkout with an empty cart")));
    }

    @Test
    void shouldReturnOrderHistoryAndReturn200Ok() throws Exception {
        when(orderService.getOrderHistory(testUser)).thenReturn(List.of(sampleOrderDto));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(orderId.toString())))
                .andExpect(jsonPath("$[0].totalAmount", is(34.99)));
    }

    @Test
    void shouldReturnOrderByIdAndReturn200Ok() throws Exception {
        when(orderService.getOrderById(testUser, orderId)).thenReturn(sampleOrderDto);

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.toString())))
                .andExpect(jsonPath("$.shippingAddress", is("123 Main St")));
    }

    @Test
    void shouldReturn404WhenOrderNotFoundById() throws Exception {
        UUID unknownOrderId = UUID.randomUUID();
        when(orderService.getOrderById(testUser, unknownOrderId))
                .thenThrow(new ResourceNotFoundException("Order not found with id: " + unknownOrderId));

        mockMvc.perform(get("/api/orders/" + unknownOrderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }
}
