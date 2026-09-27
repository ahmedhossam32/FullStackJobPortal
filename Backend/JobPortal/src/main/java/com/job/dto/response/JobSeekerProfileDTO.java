package com.job.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;

// Keeps the alphabetical JSON key order this DTO had as a class. Records serialize in component
// order, and @JsonPropertyOrder(alphabetic = true) does not override that, so the names are listed.
@JsonPropertyOrder({"dob", "email", "id", "name", "profilePicture", "resume", "resumeOriginalName", "role", "username"})
public record JobSeekerProfileDTO(
        Long id,
        String username,
        String name,
        String email,
        LocalDate dob,
        String profilePicture,
        String resume,
        String resumeOriginalName,
        String role
) implements ProfileResponseDTO {
}
