package com.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "borrowing_records", 
       indexes = {
           @Index(name = "idx_borrowing_book_id", columnList = "book_id"),
           @Index(name = "idx_borrowing_patron_id", columnList = "patron_id"),
           @Index(name = "idx_borrowing_dates", columnList = "borrow_date, return_date")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patron_id", nullable = false)
    private Patron patron;

    @Column(name = "borrow_date", nullable = false)
    @Builder.Default
    private LocalDate borrowDate = LocalDate.now();

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BorrowingStatus status = BorrowingStatus.BORROWED;

    @Column(name = "late_fee")
    @Builder.Default
    private Double lateFee = 0.0;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void calculateDueDate() {
        if (borrowDate != null && dueDate == null) {
            // Книгите се връщат след 14 дни
            dueDate = borrowDate.plus(14, ChronoUnit.DAYS);
        }
    }

    public boolean isOverdue() {
        return status == BorrowingStatus.BORROWED && 
               LocalDate.now().isAfter(dueDate);
    }

    public double calculateLateFee() {
        if (isOverdue()) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
            return daysOverdue * 0.50; // 50 цента на ден
        }
        return 0.0;
    }
}

