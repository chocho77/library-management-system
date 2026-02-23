package com.library.repository;

import com.library.model.Book;
import com.library.model.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

       
    Optional<Book> findByIsbn(String isbn);
    
    boolean existsByIsbn(String isbn);
    
    List<Book> findByStatus(BookStatus status);
    
    // Търсене по заглавие (без значение на главни/малки букви)
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    // Търсене по автор
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    // Комбинирано търсене
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
        String title, String author);
    
    // Намери всички налични книги
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE'")
    List<Book> findAllAvailableBooks();
    
    // Намери книги, публикувани между години
    List<Book> findByPublicationYearBetween(Integer startYear, Integer endYear);
    
    // Намери най-заеманите книги
    @Query("SELECT b, COUNT(br) as borrowCount " +
           "FROM Book b JOIN b.borrowingRecords br " +
           "GROUP BY b " +
           "ORDER BY borrowCount DESC")
    List<Object[]> findMostBorrowedBooks(@Param("limit") int limit);
    
    // Брой на заети книги от конкретен читател в момента
    @Query("SELECT COUNT(br) FROM BorrowingRecord br " +
           "WHERE br.patron.id = :patronId AND br.returnDate IS NULL")
    long countCurrentlyBorrowedByPatron(@Param("patronId") Long patronId);
}