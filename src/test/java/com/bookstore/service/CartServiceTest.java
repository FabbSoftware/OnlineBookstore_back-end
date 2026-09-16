package com.bookstore.service;

import com.bookstore.domain.Book;
import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.Role;
import com.bookstore.domain.User;
import com.bookstore.dto.cart.AddToCartRequest;
import com.bookstore.dto.cart.CartDto;
import com.bookstore.dto.cart.CartItemDto;
import com.bookstore.dto.cart.UpdateCartItemRequest;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.CartMapper;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Book book;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User(UUID.randomUUID(), "john@example.com", "encodedPassword", "John Doe", Role.ROLE_USER);
        book = new Book(
                UUID.randomUUID(),
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("34.99"),
                "Agile software craftsmanship",
                "978-0132350884",
                "cover.jpg",
                10
        );
        cart = new Cart(UUID.randomUUID(), user, new ArrayList<>());
    }

    @Test
    void shouldReturnCartForUserWhenCartExists() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        CartDto expectedDto = new CartDto(cart.getId(), List.of(), 0, BigDecimal.ZERO);
        when(cartMapper.toDto(cart)).thenReturn(expectedDto);

        CartDto result = cartService.getCart(user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(cart.getId());
        verify(cartRepository).findByUserId(user.getId());
    }

    @Test
    void shouldCreateNewCartWhenUserHasNoCart() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CartDto expectedDto = new CartDto(UUID.randomUUID(), List.of(), 0, BigDecimal.ZERO);
        when(cartMapper.toDto(any(Cart.class))).thenReturn(expectedDto);

        CartDto result = cartService.getCart(user);

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldAddBookToCartSuccessfully() {
        AddToCartRequest request = new AddToCartRequest(book.getId(), 2);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartItemDto itemDto = new CartItemDto(UUID.randomUUID(), book.getId(), book.getTitle(), book.getAuthor(), book.getPrice(), book.getCoverImageUrl(), 2, new BigDecimal("69.98"));
        CartDto expectedDto = new CartDto(cart.getId(), List.of(itemDto), 2, new BigDecimal("69.98"));
        when(cartMapper.toDto(any(Cart.class))).thenReturn(expectedDto);

        CartDto result = cartService.addItemToCart(user, request);

        assertThat(result).isNotNull();
        assertThat(result.totalItems()).isEqualTo(2);
        assertThat(result.totalAmount()).isEqualByComparingTo("69.98");
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowExceptionWhenAddingBookExceedingStock() {
        AddToCartRequest request = new AddToCartRequest(book.getId(), 15); // stock is 10
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> cartService.addItemToCart(user, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Requested quantity exceeds available stock");
    }

    @Test
    void shouldThrowExceptionWhenAddingNonExistentBook() {
        UUID unknownBookId = UUID.randomUUID();
        AddToCartRequest request = new AddToCartRequest(unknownBookId, 1);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(bookRepository.findById(unknownBookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart(user, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id:");
    }

    @Test
    void shouldUpdateCartItemQuantity() {
        UUID itemId = UUID.randomUUID();
        CartItem cartItem = new CartItem(itemId, cart, book, 2);
        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartItemDto updatedItemDto = new CartItemDto(itemId, book.getId(), book.getTitle(), book.getAuthor(), book.getPrice(), book.getCoverImageUrl(), 5, new BigDecimal("174.95"));
        CartDto expectedDto = new CartDto(cart.getId(), List.of(updatedItemDto), 5, new BigDecimal("174.95"));
        when(cartMapper.toDto(any(Cart.class))).thenReturn(expectedDto);

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);
        CartDto result = cartService.updateItemQuantity(user, itemId, request);

        assertThat(result).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCartItem() {
        UUID unknownItemId = UUID.randomUUID();
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        UpdateCartItemRequest request = new UpdateCartItemRequest(3);
        assertThatThrownBy(() -> cartService.updateItemQuantity(user, unknownItemId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart item not found with id:");
    }

    @Test
    void shouldRemoveCartItem() {
        UUID itemId = UUID.randomUUID();
        CartItem cartItem = new CartItem(itemId, cart, book, 2);
        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto expectedDto = new CartDto(cart.getId(), List.of(), 0, BigDecimal.ZERO);
        when(cartMapper.toDto(any(Cart.class))).thenReturn(expectedDto);

        CartDto result = cartService.removeItemFromCart(user, itemId);

        assertThat(result).isNotNull();
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldClearCart() {
        UUID itemId = UUID.randomUUID();
        CartItem cartItem = new CartItem(itemId, cart, book, 2);
        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto expectedDto = new CartDto(cart.getId(), List.of(), 0, BigDecimal.ZERO);
        when(cartMapper.toDto(any(Cart.class))).thenReturn(expectedDto);

        CartDto result = cartService.clearCart(user);

        assertThat(result).isNotNull();
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }
}
