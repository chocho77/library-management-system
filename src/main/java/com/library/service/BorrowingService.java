package com.library.service;

import com.library.dto.BorrowingRecordDTO;
import com.library.exception.BookAlreadyBorrowedException;
import com.library.exception.InvalidBorrowingOperationException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.*;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRecordRepository;
import com.library.repository.PatronRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// Добави импорти най-отгоре
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;

    // BORROW BOOK
    @Transactional
    public BorrowingRecordDTO borrowBook(Long bookId, Long patronId) {
        log.info("Borrowing book ID: {} for patron ID: {}", bookId, patronId);

        // Вземи книгата
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        // Проверка дали книгата е налична
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new BookAlreadyBorrowedException("Book is not available for borrowing");
        }

        // Вземи читателя
        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found with id: " + patronId));

        // Проверка дали читателят е активен
        if (patron.getStatus() != MembershipStatus.ACTIVE) {
            throw new IllegalStateException("Patron is not active");
        }

        // Проверка дали читателят няма закъснели книги
        List<BorrowingRecord> overdueRecords = borrowingRecordRepository
                .findByPatronIdAndReturnDateIsNull(patronId)
                .stream()
                .filter(BorrowingRecord::isOverdue)
                .toList();

        if (!overdueRecords.isEmpty()) {
            throw new IllegalStateException("Patron has overdue books. Cannot borrow new ones.");
        }

        // Създай запис за заемане
        BorrowingRecord record = BorrowingRecord.builder()
                .book(book)
                .patron(patron)
                .borrowDate(LocalDate.now())
                .status(BorrowingStatus.BORROWED)
                .build();

        // Актуализирай статуса на книгата
        book.setStatus(BookStatus.BORROWED);
        patron.setTotalBooksBorrowed(patron.getTotalBooksBorrowed() + 1);

        BorrowingRecord savedRecord = borrowingRecordRepository.save(record);
        bookRepository.save(book);
        patronRepository.save(patron);

        log.info("Book borrowed successfully. Record ID: {}", savedRecord.getId());

        return mapToDTO(savedRecord);
    }

    // RETURN BOOK
    @Transactional
    public BorrowingRecordDTO returnBook(Long bookId, Long patronId) {
        log.info("Returning book ID: {} from patron ID: {}", bookId, patronId);

        // Намери записа за заемане
        BorrowingRecord record = borrowingRecordRepository
                .findByBookIdAndReturnDateIsNull(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active borrowing record found for book id: " + bookId + " and patron id: " + patronId));

        // Проверка дали записът е за същия читател
        if (!record.getPatron().getId().equals(patronId)) {
            throw new IllegalStateException("This book was borrowed by another patron");
        }

        // Актуализирай записа
        record.setReturnDate(LocalDate.now());
        record.setActualReturnDate(LocalDate.now());
        record.setStatus(BorrowingStatus.RETURNED);

        // Изчисли late fee ако има
        if (record.isOverdue()) {
            double lateFee = record.calculateLateFee();
            record.setLateFee(lateFee);
            log.info("Late fee calculated: {} for record ID: {}", lateFee, record.getId());
        }

        // Актуализирай статуса на книгата
        Book book = record.getBook();
        book.setStatus(BookStatus.AVAILABLE);

        BorrowingRecord savedRecord = borrowingRecordRepository.save(record);
        bookRepository.save(book);

        log.info("Book returned successfully. Record ID: {}", savedRecord.getId());

        return mapToDTO(savedRecord);
    }

    // GET ACTIVE BORROWINGS FOR PATRON
    @Transactional(readOnly = true)
    public List<BorrowingRecordDTO> getActiveBorrowingsForPatron(Long patronId) {
        log.debug("Fetching active borrowings for patron ID: {}", patronId);
        
        patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found with id: " + patronId));

        return borrowingRecordRepository.findByPatronIdAndReturnDateIsNull(patronId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // GET BORROWING HISTORY FOR PATRON
    @Transactional(readOnly = true)
    public List<BorrowingRecordDTO> getBorrowingHistoryForPatron(Long patronId) {
        log.debug("Fetching borrowing history for patron ID: {}", patronId);
        
        patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found with id: " + patronId));

        return borrowingRecordRepository.findByPatronIdOrderByBorrowDateDesc(patronId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // GET CURRENT BORROWER OF BOOK
    @Transactional(readOnly = true)
    public Patron getCurrentBorrowerOfBook(Long bookId) {
        log.debug("Fetching current borrower for book ID: {}", bookId);
        
        return borrowingRecordRepository.findByBookIdAndReturnDateIsNull(bookId)
                .map(BorrowingRecord::getPatron)
                .orElse(null);
    }

    // CHECK IF BOOK IS BORROWED
    @Transactional(readOnly = true)
    public boolean isBookBorrowed(Long bookId) {
        return borrowingRecordRepository.existsByBookIdAndReturnDateIsNull(bookId);
    }

    // GET ALL OVERDUE BORROWINGS
    @Transactional(readOnly = true)
    public List<BorrowingRecordDTO> getOverdueBorrowings() {
        log.debug("Fetching all overdue borrowings");
        
        return borrowingRecordRepository
                .findByReturnDateIsNullAndDueDateBefore(LocalDate.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // SCHEDULED JOB - Проверка за закъснели книги всеки ден в 8:00
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkOverdueBooks() {
        log.info("Running scheduled job: Checking for overdue books");
        
        List<BorrowingRecord> overdueRecords = borrowingRecordRepository
                .findByReturnDateIsNullAndDueDateBefore(LocalDate.now());
        
        for (BorrowingRecord record : overdueRecords) {
            record.setStatus(BorrowingStatus.OVERDUE);
            log.warn("Book ID: {} borrowed by patron ID: {} is overdue. Due date was: {}", 
                    record.getBook().getId(), 
                    record.getPatron().getId(), 
                    record.getDueDate());
        }
        
        borrowingRecordRepository.saveAll(overdueRecords);
        log.info("Found {} overdue books", overdueRecords.size());
    }

    // SCHEDULED JOB - Изпращане на напомняния (пример)
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendDueDateReminders() {
        log.info("Running scheduled job: Sending due date reminders");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<BorrowingRecord> dueTomorrow = borrowingRecordRepository
                .findByReturnDateIsNullAndDueDateBefore(tomorrow);
        
        // Тук би изпратил имейли
        for (BorrowingRecord record : dueTomorrow) {
            log.info("Reminder: Book '{}' is due tomorrow for patron {}", 
                    record.getBook().getTitle(),
                    record.getPatron().getEmail());
        }
    }

    // Helper mapper method
    private BorrowingRecordDTO mapToDTO(BorrowingRecord record) {
        return BorrowingRecordDTO.builder()
                .id(record.getId())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .patronId(record.getPatron().getId())
                .patronName(record.getPatron().getFullName())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .lateFee(record.getLateFee())
                .isOverdue(record.isOverdue())
                .build();
    }

// Добави тези методи в BorrowingService.java

@Transactional
public BorrowingRecordDTO extendBorrowing(Long recordId, int days) {
    log.info("Extending borrowing record ID: {} by {} days", recordId, days);
    
    BorrowingRecord record = borrowingRecordRepository.findById(recordId)
            .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found with id: " + recordId));
    
    if (record.getReturnDate() != null) {
        throw new InvalidBorrowingOperationException("Cannot extend a returned book");
    }
    
    if (record.isOverdue()) {
        throw new InvalidBorrowingOperationException("Cannot extend an overdue book. Please return it first.");
    }
    
    record.setDueDate(record.getDueDate().plusDays(days));
    record.setStatus(BorrowingStatus.EXTENDED);
    
    BorrowingRecord savedRecord = borrowingRecordRepository.save(record);
    log.info("Borrowing extended successfully. New due date: {}", savedRecord.getDueDate());
    
    return mapToDTO(savedRecord);
}

public Map<String, Object> getDailyBorrowingStats() {
    log.debug("Fetching daily borrowing statistics");
    
    LocalDate today = LocalDate.now();
    
    // Тези методи трябва да ги добавиш в BorrowingRecordRepository
    long borrowedToday = borrowingRecordRepository.countByBorrowDate(today);
    long returnedToday = borrowingRecordRepository.countByReturnDate(today);
    long overdue = borrowingRecordRepository.countByReturnDateIsNullAndDueDateBefore(today);
    long activeBorrowings = borrowingRecordRepository.countByReturnDateIsNull();
    
    Map<String, Object> stats = new HashMap<>();
    stats.put("date", today.toString());
    stats.put("borrowedToday", borrowedToday);
    stats.put("returnedToday", returnedToday);
    stats.put("currentlyOverdue", overdue);
    stats.put("activeBorrowings", activeBorrowings);
    
    return stats;
}

    
}