package com.library.exception;

public class InvalidBorrowingOperationException extends RuntimeException {
    
    public InvalidBorrowingOperationException(String message) {
        super(message);
    }

    public InvalidBorrowingOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
