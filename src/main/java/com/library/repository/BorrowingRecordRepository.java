package com.library.repository;

import com.library.model.BorrowingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {
    
    // Намери текущо заета книга
    Optional<BorrowingRecord> findByBookIdAndReturnDateIsNull(Long bookId);
    
    // Провери дали книга е заета в момента
    boolean existsByBookIdAndReturnDateIsNull(Long bookId);
    
    // История на заемания за книга
    List<BorrowingRecord> findByBookIdOrderByBorrowDateDesc(Long bookId);
    
    // История на заемания за читател
    List<BorrowingRecord> findByPatronIdOrderByBorrowDateDesc(Long patronId);
    
    // Текущо заети книги от читател
    List<BorrowingRecord> findByPatronIdAndReturnDateIsNull(Long patronId);
    
    // Закъснели заемания
    List<BorrowingRecord> findByReturnDateIsNullAndDueDateBefore(LocalDate date);
    
    // Статистика за читател
    @Query("SELECT COUNT(br) FROM BorrowingRecord br " +
           "WHERE br.patron.id = :patronId")
    long countTotalBorrowedByPatron(@Param("patronId") Long patronId);
    
    @Query("SELECT COUNT(br) FROM BorrowingRecord br " +
           "WHERE br.patron.id = :patronId AND br.returnDate IS NOT NULL")
    long countReturnedByPatron(@Param("patronId") Long patronId);
    
    // Актуализирай статус при връщане
    @Modifying
    @Query("UPDATE BorrowingRecord br SET br.returnDate = :returnDate, " +
           "br.status = 'RETURNED', br.actualReturnDate = :returnDate " +
           "WHERE br.book.id = :bookId AND br.patron.id = :patronId " +
           "AND br.returnDate IS NULL")
    int returnBook(@Param("bookId") Long bookId, 
                   @Param("patronId") Long patronId,
                   @Param("returnDate") LocalDate returnDate);
}
