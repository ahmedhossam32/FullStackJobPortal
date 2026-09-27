package com.job.service.impl;

import com.job.dto.request.EmployerRegisterRequestDTO;
import com.job.dto.request.JobSeekerRegisterRequestDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;
import com.job.enums.Role;
import com.job.exception.DuplicateResourceException;
import com.job.repository.EmployerRepository;
import com.job.repository.JobSeekerRepository;
import com.job.repository.UserRepository;
import com.job.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public JobSeeker registerJobSeekerWithoutFiles(JobSeekerRegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setName(dto.name());
        jobSeeker.setUsername(dto.username());
        jobSeeker.setPassword(passwordEncoder.encode(dto.password()));
        jobSeeker.setDob(dto.dob());
        jobSeeker.setRole(Role.JOB_SEEKER);
        jobSeeker.setEmail(dto.email());
        jobSeeker.setResumeUrl(null);
        jobSeeker.setProfilePictureUrl(null);
        return jobSeekerRepository.save(jobSeeker);
    }

    @Override
    @Transactional
    public Employer registerEmployer(EmployerRegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        Employer employer = new Employer();
        employer.setName(dto.name());
        employer.setUsername(dto.username());
        employer.setPassword(passwordEncoder.encode(dto.password()));
        employer.setCompanyName(dto.companyName());
        employer.setProfilePictureUrl(null);
        employer.setRole(Role.EMPLOYER);
        employer.setEmail(dto.email());
        employer.setIndustry(dto.industry());
        return employerRepository.save(employer);
    }

}
