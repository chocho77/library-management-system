package com.library.exception;

import lombok.Getter;
import java.util.List;

@Getter  // ← Добави тази анотация
public class PatronHasOverdueBooksException extends RuntimeException {
    
    private final Long patronId;
    private final String patronName;
    private final List<String> overdueBookTitles;


    public PatronHasOverdueBooksException(Long patronId, String patronName, List<String> overdueBookTitles) {
        super(String.format("Patron '%s' (ID: %d) has %d overdue book(s)", 
              patronName, patronId, overdueBookTitles.size()));
        this.patronId = patronId;
        this.patronName = patronName;
        this.overdueBookTitles = overdueBookTitles;
    }

    public PatronHasOverdueBooksException(String message) {
        super(message);
        this.patronId = null;
        this.patronName = null;
        this.overdueBookTitles = null;
    }
}