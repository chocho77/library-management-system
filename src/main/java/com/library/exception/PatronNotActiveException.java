package com.library.exception;

import lombok.Getter;

@Getter
public class PatronNotActiveException extends RuntimeException {
    
    private final Long patronId;
    private final String patronName;

    public PatronNotActiveException(Long patronId, String patronName) {
        super(String.format("Patron '%s' (ID: %d) is not active", patronName, patronId));
        this.patronId = patronId;
        this.patronName = patronName;
    }

    public PatronNotActiveException(String message) {
        super(message);
        this.patronId = null;
        this.patronName = null;
    }
}