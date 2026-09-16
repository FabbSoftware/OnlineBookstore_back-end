package com.bookstore.config;

import com.bookstore.domain.Book;
import com.bookstore.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataInitializer(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (bookRepository.count() == 0) {
            seedBooks();
        }
    }

    private void seedBooks() {
        List<Book> books = List.of(
                new Book(
                        "Clean Code: A Handbook of Agile Software Craftsmanship",
                        "Robert C. Martin",
                        new BigDecimal("34.99"),
                        "Even bad code can function. But if code isn't clean, it can bring a development organization to its knees.",
                        "978-0132350884",
                        "https://images.unsplash.com/photo-1532012164546-f432f2e3777a?w=400",
                        25
                ),
                new Book(
                        "The Pragmatic Programmer: Your Journey To Mastery",
                        "David Thomas, Andrew Hunt",
                        new BigDecimal("39.99"),
                        "The Pragmatic Programmer is one of those rare tech books you'll read, re-read, and read again over the years.",
                        "978-0135957059",
                        "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400",
                        18
                ),
                new Book(
                        "Design Patterns: Elements of Reusable Object-Oriented Software",
                        "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides",
                        new BigDecimal("44.99"),
                        "Capturing a wealth of experience about the design of object-oriented software, four top-notch designers present a catalog of simple and succinct solutions.",
                        "978-0201633610",
                        "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400",
                        12
                ),
                new Book(
                        "Refactoring: Improving the Design of Existing Code",
                        "Martin Fowler",
                        new BigDecimal("42.50"),
                        "Refactoring is about improving the design of existing code. It is the process of changing a software system in such a way that it does not alter the external behavior.",
                        "978-0134757599",
                        "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400",
                        15
                ),
                new Book(
                        "Effective Java",
                        "Joshua Bloch",
                        new BigDecimal("38.00"),
                        "The definitive guide to best practices in the Java programming language, bringing you up to date with Java 7, 8, and 9 features.",
                        "978-0134685991",
                        "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=400",
                        20
                ),
                new Book(
                        "Domain-Driven Design: Tackling Complexity in the Heart of Software",
                        "Eric Evans",
                        new BigDecimal("47.99"),
                        "This book provides a systematic approach to domain-driven design, presenting a set of design practice examples, and techniques.",
                        "978-0321125217",
                        "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=400",
                        10
                )
        );

        bookRepository.saveAll(books);
    }
}
