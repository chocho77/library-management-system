package com.library.controller;

import com.library.dto.BorrowingRecordDTO;
import com.library.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
@Tag(name = "Borrowings", description = "Book borrowing and returning endpoints")
@CrossOrigin(origins = "*")
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping("/borrow/{bookId}/patron/{patronId}")
    @Operation(summary = "Borrow a book")
    public ResponseEntity<BorrowingRecordDTO> borrowBook(
            @PathVariable Long bookId,
            @PathVariable Long patronId) {
        BorrowingRecordDTO record = borrowingService.borrowBook(bookId, patronId);
        return ResponseEntity.ok(record);
    }

    @PutMapping("/return/{bookId}/patron/{patronId}")
    @Operation(summary = "Return a book")
    public ResponseEntity<BorrowingRecordDTO> returnBook(
            @PathVariable Long bookId,
            @PathVariable Long patronId) {
        BorrowingRecordDTO record = borrowingService.returnBook(bookId, patronId);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/patron/{patronId}/active")
    @Operation(summary = "Get active borrowings for a patron")
    public ResponseEntity<List<BorrowingRecordDTO>> getActiveBorrowingsForPatron(
            @PathVariable Long patronId) {
        List<BorrowingRecordDTO> records = borrowingService.getActiveBorrowingsForPatron(patronId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/patron/{patronId}/history")
    @Operation(summary = "Get borrowing history for a patron")
    public ResponseEntity<List<BorrowingRecordDTO>> getBorrowingHistoryForPatron(
            @PathVariable Long patronId) {
        List<BorrowingRecordDTO> records = borrowingService.getBorrowingHistoryForPatron(patronId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/book/{bookId}/current")
    @Operation(summary = "Get current borrower of a book")
    public ResponseEntity<?> getCurrentBorrowerOfBook(@PathVariable Long bookId) {
        var patron = borrowingService.getCurrentBorrowerOfBook(bookId);
        if (patron == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(patron);
    }

    @GetMapping("/book/{bookId}/is-borrowed")
    @Operation(summary = "Check if book is borrowed")
    public ResponseEntity<Boolean> isBookBorrowed(@PathVariable Long bookId) {
        boolean isBorrowed = borrowingService.isBookBorrowed(bookId);
        return ResponseEntity.ok(isBorrowed);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue borrowings")
    public ResponseEntity<List<BorrowingRecordDTO>> getOverdueBorrowings() {
        List<BorrowingRecordDTO> records = borrowingService.getOverdueBorrowings();
        return ResponseEntity.ok(records);
    }

    @PostMapping("/{recordId}/extend")
    @Operation(summary = "Extend borrowing period")
    public ResponseEntity<BorrowingRecordDTO> extendBorrowing(
            @PathVariable Long recordId,
            @RequestParam(defaultValue = "7") int days) {
        // Трябва да добавиш този метод в BorrowingService
        BorrowingRecordDTO record = borrowingService.extendBorrowing(recordId, days);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/stats/daily")
    @Operation(summary = "Get daily borrowing statistics")
    public ResponseEntity<?> getDailyStats() {
        // Трябва да добавиш този метод в BorrowingService
        var stats = borrowingService.getDailyBorrowingStats();
        return ResponseEntity.ok(stats);
    }
}