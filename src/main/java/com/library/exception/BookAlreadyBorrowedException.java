package com.library.exception;

import lombok.Getter;

@Getter
public class BookAlreadyBorrowedException extends RuntimeException {
    
    private final Long bookId;
    private final String bookTitle;

    public BookAlreadyBorrowedException(String message) {
        super(message);
        this.bookId = null;
        this.bookTitle = null;
    }

    public BookAlreadyBorrowedException(Long bookId, String bookTitle) {
        super(String.format("Book '%s' (ID: %d) is already borrowed", bookTitle, bookId));
        this.bookId = bookId;
        this.bookTitle = bookTitle;
    }

    public BookAlreadyBorrowedException(String message, Throwable cause) {
        super(message, cause);
        this.bookId = null;
        this.bookTitle = null;
    }
}