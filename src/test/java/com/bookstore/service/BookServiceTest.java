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
import java.util.UUID;

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
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Book book1 = new Book(id1, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);
        Book book2 = new Book(id2, "The Pragmatic Programmer", "David Thomas, Andrew Hunt", new BigDecimal("39.99"), "Your journey to mastery", "978-0135957059", "pragmatic.jpg", 10);
        List<Book> books = List.of(book1, book2);

        BookDto dto1 = new BookDto(id1, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);
        BookDto dto2 = new BookDto(id2, "The Pragmatic Programmer", "David Thomas, Andrew Hunt", new BigDecimal("39.99"), "Your journey to mastery", "978-0135957059", "pragmatic.jpg", 10);
        List<BookDto> dtos = List.of(dto1, dto2);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(dtos);

        List<BookDto> result = bookService.getAllBooks("");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Clean Code");
        assertThat(result.get(1).title()).isEqualTo("The Pragmatic Programmer");
    }

    @Test
    void shouldReturnFilteredBooksWhenQueryProvided() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);
        List<Book> books = List.of(book);
        BookDto dto = new BookDto(id, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);

        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("clean", "clean")).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(List.of(dto));

        List<BookDto> result = bookService.getAllBooks("clean");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Clean Code");
    }

    @Test
    void shouldReturnBookByIdWhenExists() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);
        BookDto dto = new BookDto(id, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "A handbook of agile software craftsmanship", "978-0132350884", "clean_code.jpg", 15);

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(dto);

        BookDto result = bookService.getBookById(id);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.price()).isEqualByComparingTo("34.99");
    }

    @Test
    void shouldThrowExceptionWhenBookNotFoundById() {
        UUID unknownId = UUID.randomUUID();
        when(bookRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id:");
    }
}
