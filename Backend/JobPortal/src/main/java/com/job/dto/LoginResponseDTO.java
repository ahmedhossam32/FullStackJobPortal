package com.job.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoginResponseDTO {
    private String token;
    private String username;
    private String name;
    private LocalDate dob;
    private String email;
    private String role;
    private String profilePicture;
    private String resume;
    private String resumeOriginalName;

}
