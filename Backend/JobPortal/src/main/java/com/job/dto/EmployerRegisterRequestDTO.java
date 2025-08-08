package com.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployerRegisterRequestDTO {

    private String name;
    private String username;
    private String password;
    private String companyName;
    private String profilePictureFileName;
    private String email;
    private String industry;

}
