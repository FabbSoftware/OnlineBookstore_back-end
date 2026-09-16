package com.bookstore.controller;

import com.bookstore.dto.book.BookDto;
import com.bookstore.exception.GlobalExceptionHandler;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnAllBooksAndReturn200Ok() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<BookDto> books = List.of(
                new BookDto(id1, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "Description", "978-0132350884", "cover1.jpg", 10),
                new BookDto(id2, "The Pragmatic Programmer", "David Thomas", new BigDecimal("39.99"), "Description", "978-0135957059", "cover2.jpg", 5)
        );

        when(bookService.getAllBooks(null)).thenReturn(books);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")))
                .andExpect(jsonPath("$[1].title", is("The Pragmatic Programmer")));
    }

    @Test
    void shouldReturnFilteredBooksWhenQueryParamProvided() throws Exception {
        UUID id = UUID.randomUUID();
        List<BookDto> books = List.of(
                new BookDto(id, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "Description", "978-0132350884", "cover.jpg", 10)
        );

        when(bookService.getAllBooks("clean")).thenReturn(books);

        mockMvc.perform(get("/api/books").param("q", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")));
    }

    @Test
    void shouldReturnBookByIdAndReturn200Ok() throws Exception {
        UUID id = UUID.randomUUID();
        BookDto book = new BookDto(id, "Clean Code", "Robert C. Martin", new BigDecimal("34.99"), "Description", "978-0132350884", "cover.jpg", 10);

        when(bookService.getBookById(id)).thenReturn(book);

        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.author", is("Robert C. Martin")))
                .andExpect(jsonPath("$.price", is(34.99)));
    }

    @Test
    void shouldReturn404WhenBookNotFoundById() throws Exception {
        UUID unknownId = UUID.randomUUID();

        when(bookService.getBookById(unknownId))
                .thenThrow(new ResourceNotFoundException("Book not found with id: " + unknownId));

        mockMvc.perform(get("/api/books/" + unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }
}
