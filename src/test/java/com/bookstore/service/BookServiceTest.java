package com.bookstore.service;

import com.bookstore.domain.Book;
import com.bookstore.dto.book.BookDto;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, bookMapper);
    }

    @Test
    void shouldReturnAllBooksWhenNoQueryProvided() {
        Book book1 = new Book(1L, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);
        Book book2 = new Book(2L, "The Pragmatic Programmer", "David Thomas, Andrew Hunt", new BigDecimal("39.99"), "Your journey to mastery", "978-0135957059", "pragmatic.jpg", 10);
        List<Book> books = List.of(book1, book2);

        BookDto dto1 = new BookDto(1L, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);
        BookDto dto2 = new BookDto(2L, "The Pragmatic Programmer", "David Thomas, Andrew Hunt", new BigDecimal("39.99"), "Your journey to mastery", "978-0135957059", "pragmatic.jpg", 10);
        List<BookDto> dtos = List.of(dto1, dto2);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(dtos);

        List<BookDto> result = bookService.getAllBooks(null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Clean Code");
        assertThat(result.get(1).title()).isEqualTo("The Pragmatic Programmer");
    }

    @Test
    void shouldReturnFilteredBooksWhenQueryProvided() {
        Book book = new Book(1L, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);
        List<Book> books = List.of(book);
        BookDto dto = new BookDto(1L, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);

        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("clean", "clean")).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(List.of(dto));

        List<BookDto> result = bookService.getAllBooks("clean");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Clean Code");
    }

    @Test
    void shouldReturnBookByIdWhenExists() {
        Book book = new Book(1L, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);
        BookDto dto = new BookDto(1L, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(dto);

        BookDto result = bookService.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.price()).isEqualByComparingTo("34.99");
    }

    @Test
    void shouldThrowExceptionWhenBookNotFoundById() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book not found with id: 99");
    }
}
