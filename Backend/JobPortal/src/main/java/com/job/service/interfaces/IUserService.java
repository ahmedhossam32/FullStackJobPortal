package com.job.service.interfaces;

import com.job.dto.request.EmployerRegisterRequestDTO;
import com.job.dto.request.JobSeekerRegisterRequestDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;

public interface IUserService {
    JobSeeker registerJobSeekerWithoutFiles(JobSeekerRegisterRequestDTO dto);
    Employer registerEmployer(EmployerRegisterRequestDTO dto);
}