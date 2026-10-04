package com.job.service;

import com.job.dto.request.EmployerRegisterRequestDTO;
import com.job.dto.request.JobSeekerRegisterRequestDTO;

public interface UserService {
    void registerJobSeekerWithoutFiles(JobSeekerRegisterRequestDTO dto);
    void registerEmployer(EmployerRegisterRequestDTO dto);
}
