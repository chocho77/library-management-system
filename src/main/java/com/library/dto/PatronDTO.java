package com.library.dto;

import com.library.model.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatronDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private MembershipStatus status;
    private LocalDate membershipDate;
    private Integer totalBooksBorrowed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}