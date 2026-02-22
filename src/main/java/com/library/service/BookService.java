package com.library.service;

import com.library.dto.BookDTO;
import com.library.dto.request.CreateBookRequest;
import com.library.dto.request.UpdateBookRequest;
import com.library.exception.BookAlreadyBorrowedException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.model.BorrowingRecord;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;

    // CREATE
    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public BookDTO createBook(CreateBookRequest request) {
        log.info("Creating new book: {}", request.getTitle());
        
        // Проверка за дублиращ се ISBN
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + request.getIsbn() + " already exists");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publicationYear(request.getPublicationYear())
                .publisher(request.getPublisher())
                .description(request.getDescription())
                .status(BookStatus.AVAILABLE)
                .build();

        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully with ID: {}", savedBook.getId());
        
        return mapToDTO(savedBook);
    }

    // READ - всички книги с пагинация
    @Transactional(readOnly = true)
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        log.debug("Fetching all books, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    // READ - една книга по ID (с кеширане)
    @Transactional(readOnly = true)
    @Cacheable(value = "books", key = "#id")
    public BookDTO getBookById(Long id) {
        log.debug("Fetching book with ID: {}", id);
        Book book = findBookById(id);
        return mapToDTO(book);
    }

    // READ - търсене по заглавие
    @Transactional(readOnly = true)
    public List<BookDTO> searchBooksByTitle(String title) {
        log.debug("Searching books by title: {}", title);
        return bookRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // READ - търсене по автор
    @Transactional(readOnly = true)
    public List<BookDTO> searchBooksByAuthor(String author) {
        log.debug("Searching books by author: {}", author);
        return bookRepository.findByAuthorContainingIgnoreCase(author)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // READ - налични книги
    @Transactional(readOnly = true)
    public List<BookDTO> getAvailableBooks() {
        log.debug("Fetching all available books");
        return bookRepository.findAllAvailableBooks()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public BookDTO updateBook(Long id, UpdateBookRequest request) {
        log.info("Updating book with ID: {}", id);
        
        Book book = findBookById(id);
        
        // Проверка за ISBN само ако се променя
        if (request.getIsbn() != null && !request.getIsbn().equals(book.getIsbn())) {
            if (bookRepository.existsByIsbn(request.getIsbn())) {
                throw new IllegalArgumentException("Book with ISBN " + request.getIsbn() + " already exists");
            }
            book.setIsbn(request.getIsbn());
        }

        // Актуализиране само на непразни полета
        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());
        if (request.getPublicationYear() != null) book.setPublicationYear(request.getPublicationYear());
        if (request.getPublisher() != null) book.setPublisher(request.getPublisher());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getStatus() != null) book.setStatus(request.getStatus());

        Book updatedBook = bookRepository.save(book);
        log.info("Book updated successfully with ID: {}", updatedBook.getId());
        
        return mapToDTO(updatedBook);
    }

    // UPDATE - промяна на статус
    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public BookDTO updateBookStatus(Long id, BookStatus status) {
        log.info("Updating status of book ID: {} to {}", id, status);
        
        Book book = findBookById(id);
        book.setStatus(status);
        
        Book updatedBook = bookRepository.save(book);
        log.info("Book status updated successfully");
        
        return mapToDTO(updatedBook);
    }

    // DELETE
    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);
        
        Book book = findBookById(id);
        
        // Проверка дали книгата е заета в момента
        if (book.getStatus() == BookStatus.BORROWED) {
            throw new BookAlreadyBorrowedException("Cannot delete book that is currently borrowed");
        }
        
        bookRepository.delete(book);
        log.info("Book deleted successfully with ID: {}", id);
    }

    // READ - история на заеманията на книга
    @Transactional(readOnly = true)
    public List<BorrowingRecord> getBookBorrowingHistory(Long bookId) {
        log.debug("Fetching borrowing history for book ID: {}", bookId);
        findBookById(bookId); // Проверка дали книгата съществува
        return borrowingRecordRepository.findByBookIdOrderByBorrowDateDesc(bookId);
    }

    // READ - най-заемани книги
    @Transactional(readOnly = true)
    public List<Object[]> getMostBorrowedBooks(int limit) {
        log.debug("Fetching top {} most borrowed books", limit);
        return bookRepository.findMostBorrowedBooks(limit);
    }

    // Helper метод
    private Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    // Mapper метод
    private BookDTO mapToDTO(Book book) {
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publicationYear(book.getPublicationYear())
                .publisher(book.getPublisher())
                .status(book.getStatus())
                .description(book.getDescription())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}