// LoginEmployerResponseDTO.java
package com.job.dto;

import lombok.Data;

@Data
public class LoginEmployerResponseDTO {
    private String token;
    private String username;
    private String name;
    private String email;
    private String role;
    private String companyName;
    private String industry;
    private String profilePicture;
}
