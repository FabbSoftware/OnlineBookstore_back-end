package com.bookstore.controller;

import com.bookstore.domain.Role;
import com.bookstore.domain.User;
import com.bookstore.dto.cart.AddToCartRequest;
import com.bookstore.dto.cart.CartDto;
import com.bookstore.dto.cart.CartItemDto;
import com.bookstore.dto.cart.UpdateCartItemRequest;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.GlobalExceptionHandler;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private User testUser;
    private CartDto sampleCartDto;
    private UUID cartId;
    private UUID itemId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        testUser = new User(UUID.randomUUID(), "user@example.com", "pass", "Jane Doe", Role.ROLE_USER);
        cartId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        CartItemDto itemDto = new CartItemDto(
                itemId,
                bookId,
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("34.99"),
                "clean.jpg",
                2,
                new BigDecimal("69.98")
        );
        sampleCartDto = new CartDto(cartId, List.of(itemDto), 2, new BigDecimal("69.98"));

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testUser;
            }
        };

        mockMvc = MockMvcBuilders
                .standaloneSetup(cartController)
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnCartAndReturn200Ok() throws Exception {
        when(cartService.getCart(testUser)).thenReturn(sampleCartDto);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(cartId.toString())))
                .andExpect(jsonPath("$.totalItems", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(69.98)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].bookTitle", is("Clean Code")));
    }

    @Test
    void shouldAddItemToCartAndReturn200Ok() throws Exception {
        AddToCartRequest request = new AddToCartRequest(bookId, 2);
        when(cartService.addItemToCart(eq(testUser), any(AddToCartRequest.class))).thenReturn(sampleCartDto);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(cartId.toString())))
                .andExpect(jsonPath("$.totalItems", is(2)));
    }

    @Test
    void shouldReturn400WhenAddToCartPayloadIsInvalid() throws Exception {
        AddToCartRequest invalidRequest = new AddToCartRequest(null, 0);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void shouldReturn400WhenAddToCartExceedsStock() throws Exception {
        AddToCartRequest request = new AddToCartRequest(bookId, 50);
        when(cartService.addItemToCart(eq(testUser), any(AddToCartRequest.class)))
                .thenThrow(new BadRequestException("Requested quantity exceeds available stock"));

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Requested quantity exceeds available stock")));
    }

    @Test
    void shouldUpdateCartItemQuantityAndReturn200Ok() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(3);
        when(cartService.updateItemQuantity(eq(testUser), eq(itemId), any(UpdateCartItemRequest.class)))
                .thenReturn(sampleCartDto);

        mockMvc.perform(put("/api/cart/items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(cartId.toString())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentCartItem() throws Exception {
        UUID unknownItemId = UUID.randomUUID();
        UpdateCartItemRequest request = new UpdateCartItemRequest(3);
        when(cartService.updateItemQuantity(eq(testUser), eq(unknownItemId), any(UpdateCartItemRequest.class)))
                .thenThrow(new ResourceNotFoundException("Cart item not found with id: " + unknownItemId));

        mockMvc.perform(put("/api/cart/items/" + unknownItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void shouldRemoveCartItemAndReturn200Ok() throws Exception {
        CartDto emptyCart = new CartDto(cartId, List.of(), 0, BigDecimal.ZERO);
        when(cartService.removeItemFromCart(testUser, itemId)).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/cart/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems", is(0)));
    }

    @Test
    void shouldClearCartAndReturn200Ok() throws Exception {
        CartDto emptyCart = new CartDto(cartId, List.of(), 0, BigDecimal.ZERO);
        when(cartService.clearCart(testUser)).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems", is(0)));
    }
}
