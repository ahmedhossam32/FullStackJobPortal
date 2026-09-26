package com.job.service;

import com.job.dto.request.EmployerRegisterRequestDTO;
import com.job.dto.request.JobSeekerRegisterRequestDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;

public interface UserService {
    JobSeeker registerJobSeekerWithoutFiles(JobSeekerRegisterRequestDTO dto);
    Employer registerEmployer(EmployerRegisterRequestDTO dto);
}
