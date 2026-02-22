package com.library.exception;

public class PatronNotFoundException extends ResourceNotFoundException {
    
    public PatronNotFoundException(Long id) {
        super("Patron", "id", id);
    }

    public PatronNotFoundException(String email) {
        super("Patron", "email", email);
    }
}
