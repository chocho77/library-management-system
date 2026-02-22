package com.library.dto.request;

import com.library.model.BookStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {
    
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;
    
    @Size(min = 2, max = 100, message = "Author name must be between 2 and 100 characters")
    private String author;
    
    @Pattern(regexp = "^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)97[89][- ]?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9]$",
             message = "Invalid ISBN format")
    private String isbn;
    
    @Min(value = 1000, message = "Publication year must be valid")
    @Max(value = 2026, message = "Publication year cannot be in the future")
    private Integer publicationYear;
    
    @Size(max = 100, message = "Publisher name must not exceed 100 characters")
    private String publisher;
    
    private String description;
    
    private BookStatus status;
}
