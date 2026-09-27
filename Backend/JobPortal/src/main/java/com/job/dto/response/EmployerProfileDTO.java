package com.job.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// Keeps the alphabetical JSON key order this DTO had as a class. Records serialize in component
// order, and @JsonPropertyOrder(alphabetic = true) does not override that, so the names are listed.
@JsonPropertyOrder({"companyName", "email", "id", "industry", "name", "profilePicture", "role", "username"})
public record EmployerProfileDTO(
        Long id,
        String username,
        String name,
        String email,
        String companyName,
        String industry,
        String profilePicture,
        String role
) implements ProfileResponseDTO {
}
