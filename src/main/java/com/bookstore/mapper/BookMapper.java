package com.bookstore.mapper;

import com.bookstore.domain.Book;
import com.bookstore.dto.book.BookDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDto toDto(Book book);
    Book toEntity(BookDto dto);
    List<BookDto> toDtoList(List<Book> books);
}
