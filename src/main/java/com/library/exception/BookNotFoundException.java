package com.library.exception;

public class BookNotFoundException extends ResourceNotFoundException {
    
    public BookNotFoundException(Long id) {
        super("Book", "id", id);
    }

    public BookNotFoundException(String isbn) {
        super("Book", "ISBN", isbn);
    }
}
