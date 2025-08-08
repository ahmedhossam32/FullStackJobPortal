package com.job.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;


@Getter
@Setter
public class JobSeekerRegisterRequestDTO {
    private String name;
    private String username;
    private String password;
    private LocalDate dob;
    private String email;
}
