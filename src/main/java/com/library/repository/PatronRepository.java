package com.library.repository;

import com.library.model.Patron;
import com.library.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatronRepository extends JpaRepository<Patron, Long> {

       // Добави този метод в PatronRepository.java
// (провери дали вече го имаш, ако няма - добави го)

    
    Optional<Patron> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<Patron> findByStatus(MembershipStatus status);
    
    List<Patron> findByLastNameContainingIgnoreCase(String lastName);
    
    @Query("SELECT p FROM Patron p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Patron> searchPatrons(@Param("searchTerm") String searchTerm);
    
    // Намери читатели със закъснели книги
    @Query("SELECT DISTINCT p FROM Patron p " +
           "JOIN p.borrowingRecords br " +
           "WHERE br.returnDate IS NULL AND br.dueDate < CURRENT_DATE")
    List<Patron> findPatronsWithOverdueBooks();
    
    @Query("SELECT COUNT(p) FROM Patron p WHERE p.status = :status")
    long countByStatus(@Param("status") MembershipStatus status);
}