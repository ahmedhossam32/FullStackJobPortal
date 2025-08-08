package com.job.service;

import com.job.dto.*;
import com.job.entity.Employer;
import com.job.entity.Job;
import com.job.entity.JobSeeker;
import com.job.enums.Role;
import com.job.entity.User;
import com.job.repository.JobRepository;
import com.job.repository.UserRepository;
import com.job.repository.EmployerRepository;
import com.job.repository.JobSeekerRepository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JobRepository jobRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public JobSeeker registerJobSeeker(JobSeekerRegisterRequestDTO dto, MultipartFile resume, MultipartFile profilePicture) throws IOException {
        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setName(dto.getName());
        jobSeeker.setUsername(dto.getUsername());
        jobSeeker.setPassword(passwordEncoder.encode(dto.getPassword()));
        jobSeeker.setDob(dto.getDob());
        jobSeeker.setRole(Role.JOB_SEEKER);
        jobSeeker.setEmail(dto.getEmail());


        if (resume != null && !resume.isEmpty()) {
            String originalFilename = resume.getOriginalFilename();
            String resumeName = saveFile(resume, "uploads/resumes/");

            jobSeeker.setResumeFileName(resumeName);
            jobSeeker.setResumeOriginalName(originalFilename);
        }


        if (profilePicture != null && !profilePicture.isEmpty()) {
            String pictureName = saveFile(profilePicture, "uploads/profile-pictures/");
            jobSeeker.setProfilePictureFileName(pictureName);
        }

        return jobSeekerRepository.save(jobSeeker);
    }

    private String saveFile(MultipartFile file, String uploadDir) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }



    public Employer registerEmployer(EmployerRegisterRequestDTO dto) {
        Employer employer = new Employer();
        employer.setName(dto.getName());
        employer.setUsername(dto.getUsername());
        employer.setPassword(passwordEncoder.encode(dto.getPassword()));
        employer.setCompanyName(dto.getCompanyName());
        employer.setProfilePictureFileName(dto.getProfilePictureFileName());
        employer.setRole(Role.EMPLOYER);
        employer.setEmail(dto.getEmail());
        employer.setIndustry(dto.getIndustry());
        System.out.println("Employer Usernamee " +  employer.getUsername());
        return employerRepository.save(employer);
    }

    public boolean authenticate(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
    public User getUserByUsername(@NotBlank(message = "Username is required") String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public String uploadResume(MultipartFile file, JobSeeker jobSeeker) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "resumes" + File.separator;
            File dest = new File(uploadDir + fileName);

            dest.getParentFile().mkdirs(); // Create dir if missing
            file.transferTo(dest);

            jobSeeker.setResumeFileName(fileName);
            userRepository.save(jobSeeker); // Assuming single UserRepository

            return "/uploads/resumes/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Resume upload failed", e);
        }
    }

    public JobSeeker registerJobSeekerWithoutFiles(JobSeekerRegisterRequestDTO dto) {
        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setName(dto.getName());
        jobSeeker.setUsername(dto.getUsername());
        jobSeeker.setPassword(passwordEncoder.encode(dto.getPassword()));
        jobSeeker.setDob(dto.getDob());
        jobSeeker.setRole(Role.JOB_SEEKER);
        jobSeeker.setEmail(dto.getEmail());

        // Skip resume and profile picture
        jobSeeker.setResumeFileName(null);
        jobSeeker.setProfilePictureFileName(null);

        return jobSeekerRepository.save(jobSeeker);
    }

    public String uploadProfilePicture(MultipartFile file, User jobSeeker) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "profile-pictures" + File.separator;
            File dest = new File(uploadDir + fileName);

            dest.getParentFile().mkdirs();
            file.transferTo(dest);

            jobSeeker.setProfilePictureFileName(fileName);
            userRepository.save(jobSeeker);
            return "/uploads/profile-pictures/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Profile picture upload failed", e);
        }
    }

    @Transactional
    public void saveJob(User user, Long jobId) {
        if (!(user instanceof JobSeeker)) {
            throw new RuntimeException("Only job seekers can save jobs.");
        }

        Long jobSeekerId = user.getId();
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new RuntimeException("Job seeker not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (jobSeeker.getSavedJobs().contains(job)) {
            throw new IllegalStateException("You already saved this job.");
        }

        jobSeeker.getSavedJobs().add(job);
        jobSeekerRepository.save(jobSeeker);
    }



    @Transactional
    public void unsaveJob(User user, Long jobId) {
        if (!(user instanceof JobSeeker)) {
            throw new RuntimeException("Only job seekers can unsave jobs.");
        }

        Long jobSeekerId = user.getId();
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new RuntimeException("Job seeker not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!jobSeeker.getSavedJobs().contains(job)) {
            throw new IllegalStateException("This job is not in your saved list.");
        }

        jobSeeker.getSavedJobs().remove(job);
        jobSeekerRepository.save(jobSeeker);
    }


    @Transactional(readOnly = true)
    public List<JobResponseDTO> getSavedJobs(User user) {
        if (!(user instanceof JobSeeker)) {
            throw new RuntimeException("Only job seekers can view saved jobs.");
        }

        Long jobSeekerId = user.getId();

        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new RuntimeException("Job seeker not found"));

        return jobSeeker.getSavedJobs().stream()
                .map(this::mapToDTO)
                .toList();
    }


    public JobResponseDTO mapToDTO(Job job) {
        JobResponseDTO dto = new JobResponseDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setLocation(job.getLocation());
        dto.setPostedAt(job.getPostedAt());
        dto.setCompanyName(job.getEmployer().getCompanyName());
        dto.setProfilePicture(job.getEmployer().getProfilePictureFileName());
        dto.setType(job.getType());
        dto.setWorkMode(job.getWorkMode());
        dto.setResponsibilities(job.getResponsibilities());
        dto.setRequiredSkills(job.getRequiredSkills());
        return dto;
    }


    public void updateJobSeekerProfile(JobSeeker currentUser, JobSeeker updatedInfo) {
        currentUser.setName(updatedInfo.getName());
        currentUser.setUsername(updatedInfo.getUsername());
        currentUser.setEmail(updatedInfo.getEmail());
        currentUser.setDob(updatedInfo.getDob());
        userRepository.save(currentUser);
    }

    public LoginEmployerResponseDTO buildEmployerResponse(Employer employer, String token) {
        LoginEmployerResponseDTO response = new LoginEmployerResponseDTO();
        response.setToken(token);
        response.setUsername(employer.getUsername());
        response.setName(employer.getName());
        response.setEmail(employer.getEmail());
        response.setRole(employer.getRole().name());
        response.setCompanyName(employer.getCompanyName());
        response.setIndustry(employer.getIndustry());
        response.setProfilePicture(employer.getProfilePictureFileName());
        return response;
    }

    public LoginResponseDTO buildJobSeekerResponse(JobSeeker jobSeeker, String token) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUsername(jobSeeker.getUsername());
        response.setName(jobSeeker.getName());
        response.setDob(jobSeeker.getDob());
        response.setEmail(jobSeeker.getEmail());
        response.setRole(jobSeeker.getRole().name());
        response.setProfilePicture(jobSeeker.getProfilePictureFileName());
        response.setResume(jobSeeker.getResumeFileName());
        response.setResumeOriginalName(jobSeeker.getResumeOriginalName());
        return response;
    }

    public Object getCurrentUserDto(User user) {
        if (user.getRole() == Role.JOB_SEEKER && user instanceof JobSeeker jobSeeker) {
            LoginResponseDTO dto = new LoginResponseDTO();
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setName(user.getName());
            dto.setRole(user.getRole().name());
            dto.setProfilePicture(user.getProfilePictureFileName());
            dto.setResume(jobSeeker.getResumeFileName());
            dto.setResumeOriginalName(jobSeeker.getResumeOriginalName());
            dto.setDob(jobSeeker.getDob());
            return dto;
        }

        if (user.getRole() == Role.EMPLOYER && user instanceof Employer employer) {
            LoginEmployerResponseDTO dto = new LoginEmployerResponseDTO();
            dto.setUsername(employer.getUsername());
            dto.setEmail(employer.getEmail());
            dto.setName(employer.getName());
            dto.setRole(employer.getRole().name());
            dto.setCompanyName(employer.getCompanyName());
            dto.setIndustry(employer.getIndustry());
            dto.setProfilePicture(employer.getProfilePictureFileName());
            return dto;
        }

        throw new RuntimeException("Unsupported user role");
    }


}
