package com.library.controller;

import com.library.dto.PatronDTO;
import com.library.dto.PatronStatistics;
import com.library.dto.request.CreatePatronRequest;
import com.library.dto.request.UpdatePatronRequest;
import com.library.model.MembershipStatus;
import com.library.service.PatronService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patrons")
@RequiredArgsConstructor
@Tag(name = "Patrons", description = "Patron management endpoints")
@CrossOrigin(origins = "*")
public class PatronController {

    private final PatronService patronService;

    @PostMapping
    @Operation(summary = "Create a new patron")
    public ResponseEntity<PatronDTO> createPatron(@Valid @RequestBody CreatePatronRequest request) {
        PatronDTO createdPatron = patronService.createPatron(request);
        return new ResponseEntity<>(createdPatron, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all patrons")
    public ResponseEntity<List<PatronDTO>> getAllPatrons() {
        List<PatronDTO> patrons = patronService.getAllPatrons();
        return ResponseEntity.ok(patrons);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patron by ID")
    public ResponseEntity<PatronDTO> getPatronById(@PathVariable Long id) {
        PatronDTO patron = patronService.getPatronById(id);
        return ResponseEntity.ok(patron);
    }

    @GetMapping("/search")
    @Operation(summary = "Search patrons")
    public ResponseEntity<List<PatronDTO>> searchPatrons(
            @RequestParam String term) {
        List<PatronDTO> patrons = patronService.searchPatrons(term);
        return ResponseEntity.ok(patrons);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get patrons with overdue books")
    public ResponseEntity<List<PatronDTO>> getPatronsWithOverdueBooks() {
        List<PatronDTO> patrons = patronService.getPatronsWithOverdueBooks();
        return ResponseEntity.ok(patrons);
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get patron statistics")
    public ResponseEntity<PatronStatistics> getPatronStatistics(@PathVariable Long id) {
        PatronStatistics statistics = patronService.getPatronStatistics(id);
        return ResponseEntity.ok(statistics);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patron by ID")
    public ResponseEntity<PatronDTO> updatePatron(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePatronRequest request) {
        PatronDTO updatedPatron = patronService.updatePatron(id, request);
        return ResponseEntity.ok(updatedPatron);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update patron status")
    public ResponseEntity<PatronDTO> updatePatronStatus(
            @PathVariable Long id,
            @RequestParam MembershipStatus status) {
        PatronDTO updatedPatron = patronService.updatePatronStatus(id, status);
        return ResponseEntity.ok(updatedPatron);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete patron by ID")
    public ResponseEntity<Void> deletePatron(@PathVariable Long id) {
        patronService.deletePatron(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get patron by email")
    public ResponseEntity<PatronDTO> getPatronByEmail(@PathVariable String email) {
        // Трябва да добавиш този метод в PatronService
        PatronDTO patron = patronService.getPatronByEmail(email);
        return ResponseEntity.ok(patron);
    }

    @GetMapping("/active/count")
    @Operation(summary = "Count active patrons")
    public ResponseEntity<Long> countActivePatrons() {
        // Трябва да добавиш този метод в PatronService
        long count = patronService.countActivePatrons();
        return ResponseEntity.ok(count);
    }
}