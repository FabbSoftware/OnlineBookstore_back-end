package com.bookstore.config;

import com.bookstore.domain.Book;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void shouldSeedBooksWhenDatabaseIsEmpty() throws Exception {
        when(bookRepository.count()).thenReturn(0L);

        dataInitializer.run();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Book>> captor = ArgumentCaptor.forClass(List.class);
        verify(bookRepository).saveAll(captor.capture());

        List<Book> savedBooks = captor.getValue();
        assertThat(savedBooks).isNotEmpty();
        assertThat(savedBooks).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void shouldNotSeedBooksWhenDatabaseIsNotEmpty() throws Exception {
        when(bookRepository.count()).thenReturn(5L);

        dataInitializer.run();

        verify(bookRepository, never()).saveAll(any());
    }
}
