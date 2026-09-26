package com.job.dto.request;

import com.job.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record JobSeekerRegisterRequestDTO(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @MaxUtf8Bytes(value = 72, message = "Password must be at most 72 bytes long")
        String password,

        @NotNull(message = "Date of birth is required")
        LocalDate dob,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
