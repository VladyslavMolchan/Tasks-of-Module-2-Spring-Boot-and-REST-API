package org.bookApi.repository;

import org.bookApi.entity.Author;
import org.bookApi.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByAuthor(Author author, Pageable pageable);
    Page<Book> findByAuthorAndYearPublished(Author author, int yearPublished, Pageable pageable);
    Page<Book> findByAuthorAndTitleContainingIgnoreCase(Author author, String title, Pageable pageable);
    Page<Book> findByAuthorAndTitleContainingIgnoreCaseAndYearPublished(Author author, String title, int yearPublished, Pageable pageable);
    List<Book> findByAuthor(Author author);
}

