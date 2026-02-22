package com.library.service;

import com.library.dto.PatronDTO;
import com.library.dto.request.CreatePatronRequest;
import com.library.dto.request.UpdatePatronRequest;
import com.library.dto.PatronStatistics;
import com.library.exception.ResourceNotFoundException;
import com.library.model.MembershipStatus;
import com.library.model.Patron;
import com.library.repository.PatronRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatronService {

    private final PatronRepository patronRepository;

    // CREATE
    @Transactional
    public PatronDTO createPatron(CreatePatronRequest request) {
        log.info("Creating new patron: {} {}", request.getFirstName(), request.getLastName());
        
        // Проверка за дублиращ се email
        if (patronRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Patron with email " + request.getEmail() + " already exists");
        }

        Patron patron = Patron.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .status(MembershipStatus.ACTIVE)
                .build();

        Patron savedPatron = patronRepository.save(patron);
        log.info("Patron created successfully with ID: {}", savedPatron.getId());
        
        return mapToDTO(savedPatron);
    }

    // READ - всички читатели
    @Transactional(readOnly = true)
    public List<PatronDTO> getAllPatrons() {
        log.debug("Fetching all patrons");
        return patronRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // READ - един читател по ID
    @Transactional(readOnly = true)
    @Cacheable(value = "patrons", key = "#id")
    public PatronDTO getPatronById(Long id) {
        log.debug("Fetching patron with ID: {}", id);
        Patron patron = findPatronById(id);
        return mapToDTO(patron);
    }

    // READ - търсене на читатели
    @Transactional(readOnly = true)
    public List<PatronDTO> searchPatrons(String searchTerm) {
        log.debug("Searching patrons with term: {}", searchTerm);
        return patronRepository.searchPatrons(searchTerm)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // READ - читатели със закъснели книги
    @Transactional(readOnly = true)
    public List<PatronDTO> getPatronsWithOverdueBooks() {
        log.debug("Fetching patrons with overdue books");
        return patronRepository.findPatronsWithOverdueBooks()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    @CacheEvict(value = "patrons", key = "#id")
    public PatronDTO updatePatron(Long id, UpdatePatronRequest request) {
        log.info("Updating patron with ID: {}", id);
        
        Patron patron = findPatronById(id);
        
        // Проверка за email само ако се променя
        if (request.getEmail() != null && !request.getEmail().equals(patron.getEmail())) {
            if (patronRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Patron with email " + request.getEmail() + " already exists");
            }
            patron.setEmail(request.getEmail());
        }

        // Актуализиране само на непразни полета
        if (request.getFirstName() != null) patron.setFirstName(request.getFirstName());
        if (request.getLastName() != null) patron.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) patron.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) patron.setAddress(request.getAddress());
        if (request.getStatus() != null) patron.setStatus(request.getStatus());

        Patron updatedPatron = patronRepository.save(patron);
        log.info("Patron updated successfully with ID: {}", updatedPatron.getId());
        
        return mapToDTO(updatedPatron);
    }

    // UPDATE - статус на членство
    @Transactional
    @CacheEvict(value = "patrons", key = "#id")
    public PatronDTO updatePatronStatus(Long id, MembershipStatus status) {
        log.info("Updating status of patron ID: {} to {}", id, status);
        
        Patron patron = findPatronById(id);
        patron.setStatus(status);
        
        Patron updatedPatron = patronRepository.save(patron);
        log.info("Patron status updated successfully");
        
        return mapToDTO(updatedPatron);
    }

    // DELETE
    @Transactional
    @CacheEvict(value = "patrons", key = "#id")
    public void deletePatron(Long id) {
        log.info("Deleting patron with ID: {}", id);
        
        Patron patron = findPatronById(id);
        
        // Проверка дали читателят има незавърнати книги
        long currentlyBorrowed = patron.getBorrowingRecords()
                .stream()
                .filter(record -> record.getReturnDate() == null)
                .count();
        
        if (currentlyBorrowed > 0) {
            throw new IllegalStateException("Cannot delete patron with currently borrowed books");
        }
        
        patronRepository.delete(patron);
        log.info("Patron deleted successfully with ID: {}", id);
    }

    // READ - статистика за читател
    @Transactional(readOnly = true)
    public PatronStatistics getPatronStatistics(Long id) {
        Patron patron = findPatronById(id);
        
        long totalBorrowed = patron.getBorrowingRecords().size();
        long currentlyBorrowed = patron.getBorrowingRecords()
                .stream()
                .filter(record -> record.getReturnDate() == null)
                .count();
        long overdue = patron.getBorrowingRecords()
                .stream()
                .filter(record -> record.getReturnDate() == null && record.isOverdue())
                .count();
        
        return PatronStatistics.builder()
                .patronId(id)
                .fullName(patron.getFullName())
                .totalBooksBorrowed(totalBorrowed)
                .currentlyBorrowed(currentlyBorrowed)
                .overdueBooks(overdue)
                .membershipStatus(patron.getStatus())
                .membershipDate(patron.getMembershipDate())
                .build();
    }

    // Helper метод
    private Patron findPatronById(Long id) {
        return patronRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found with id: " + id));
    }

    // Mapper метод
    private PatronDTO mapToDTO(Patron patron) {
        return PatronDTO.builder()
                .id(patron.getId())
                .firstName(patron.getFirstName())
                .lastName(patron.getLastName())
                .fullName(patron.getFullName())
                .email(patron.getEmail())
                .phoneNumber(patron.getPhoneNumber())
                .address(patron.getAddress())
                .status(patron.getStatus())
                .membershipDate(patron.getMembershipDate())
                .totalBooksBorrowed(patron.getTotalBooksBorrowed())
                .createdAt(patron.getCreatedAt())
                .updatedAt(patron.getUpdatedAt())
                .build();
    }
}