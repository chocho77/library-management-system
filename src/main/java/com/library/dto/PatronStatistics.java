package com.library.dto;

import com.library.model.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatronStatistics {
    private Long patronId;
    private String fullName;
    private long totalBooksBorrowed;
    private long currentlyBorrowed;
    private long overdueBooks;
    private MembershipStatus membershipStatus;
    private LocalDate membershipDate;
}
