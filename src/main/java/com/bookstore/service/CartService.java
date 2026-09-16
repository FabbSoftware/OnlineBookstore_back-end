package com.bookstore.service;

import com.bookstore.domain.Book;
import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.User;
import com.bookstore.dto.cart.AddToCartRequest;
import com.bookstore.dto.cart.CartDto;
import com.bookstore.dto.cart.UpdateCartItemRequest;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.CartMapper;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final CartMapper cartMapper;

    public CartService(CartRepository cartRepository, BookRepository bookRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
        this.cartMapper = cartMapper;
    }

    @Transactional(readOnly = true)
    public CartDto getCart(User user) {
        Cart cart = getOrCreateCartEntity(user);
        return cartMapper.toDto(cart);
    }

    public CartDto addItemToCart(User user, AddToCartRequest request) {
        Cart cart = getOrCreateCartEntity(user);

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.bookId()));

        int existingQuantity = cart.getItems().stream()
                .filter(item -> item.getBook().getId().equals(book.getId()))
                .mapToInt(CartItem::getQuantity)
                .findFirst()
                .orElse(0);

        int totalRequestedQuantity = existingQuantity + request.quantity();
        if (totalRequestedQuantity > book.getStockQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock (" + book.getStockQuantity() + ")");
        }

        cart.addItem(book, request.quantity());
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }

    public CartDto updateItemQuantity(User user, UUID itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCartEntity(user);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId() != null && i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (request.quantity() > item.getBook().getStockQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock (" + item.getBook().getStockQuantity() + ")");
        }

        item.setQuantity(request.quantity());
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }

    public CartDto removeItemFromCart(User user, UUID itemId) {
        Cart cart = getOrCreateCartEntity(user);
        cart.removeItem(itemId);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }

    public CartDto clearCart(User user) {
        Cart cart = getOrCreateCartEntity(user);
        cart.clear();
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }

    public Cart getOrCreateCartEntity(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }
}
