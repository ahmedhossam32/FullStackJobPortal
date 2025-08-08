package com.job.service;

import com.job.designpatterns.strategy.*;
import com.job.dto.JobRequestDTO;
import com.job.dto.JobResponseDTO;
import com.job.entity.Employer;
import com.job.entity.Job;
import com.job.enums.JobType;
import com.job.enums.SortType;
import com.job.enums.WorkMode;
import com.job.repository.EmployerRepository;
import com.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobSortContext  jobSortContext;
    private final EmployerRepository employerRepository;


    public Job createJob(JobRequestDTO dto, Employer employer) {
        Job job = new Job();

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setType(dto.getType() != null ? dto.getType() : JobType.FULL_TIME); // Default type
        job.setWorkMode(dto.getWorkMode() != null ? dto.getWorkMode() : WorkMode.HYBRID); // Default work mode
        job.setResponsibilities(dto.getResponsibilities());
        job.setRequiredSkills(dto.getRequiredSkills());
        job.setScreeningQuestions(dto.getScreeningQuestions()); // Optional field
        job.setPostedAt(LocalDateTime.now());
        job.setEmployer(employer);

       return jobRepository.save(job);
    }

    public List<JobResponseDTO> getAllJobsSortedByDate() {
        List<Job> jobs = jobRepository.findAll();

        JobSorter sorter = new JobSorter();
        sorter.setStrategy(new SortByDateStrategy());

        List<Job> sorted = sorter.sortJobs(jobs);
        return sorted.stream().map(this::mapToDTO).toList();
    }

    public List<JobResponseDTO> searchByTitle(String keyword) {
        List<Job> jobs = jobRepository.findAll();

        JobSorter sorter = new JobSorter();
        sorter.setStrategy(new SortByTitleStrategy(keyword));

        List<Job> filtered = sorter.sortJobs(jobs);
        return filtered.stream().map(this::mapToDTO).toList();
    }

    public List<JobResponseDTO> searchByType(String type) {
        List<Job> jobs = jobRepository.findAll();

        try {
            JobSorter sorter = new JobSorter();
            sorter.setStrategy(new SortByTypeStrategy(type));

            List<Job> filtered = sorter.sortJobs(jobs);
            return filtered.stream().map(this::mapToDTO).toList();
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid job type: " + type);
            return List.of(); // return empty list on bad type input
        }
    }


    public JobResponseDTO getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return mapToDTO(job);
    }

    public JobResponseDTO updateJob(Long id, JobRequestDTO dto, Employer employer) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("You are not authorized to update this job");
        }

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setType(dto.getType());
        job.setWorkMode(dto.getWorkMode());
        job.setRequiredSkills(dto.getRequiredSkills());
        job.setResponsibilities(dto.getResponsibilities());

        Job updated = jobRepository.save(job);
        return mapToDTO(updated);
    }



    private JobResponseDTO mapToDTO(Job job) {
        JobResponseDTO dto = new JobResponseDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setLocation(job.getLocation());
        dto.setPostedAt(job.getPostedAt());
        dto.setCompanyName(job.getEmployer().getCompanyName());
        dto.setType(job.getType());
        dto.setWorkMode(job.getWorkMode());
        dto.setRequiredSkills(job.getRequiredSkills());
        dto.setResponsibilities(job.getResponsibilities());
        dto.setProfilePicture(job.getEmployer().getProfilePictureFileName());
        dto.setScreeningQuestions(job.getScreeningQuestions());
        return dto;
    }

    public void deleteJob(Long jobId, Employer employer) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("You are not authorized to delete this job");
        }

        jobRepository.delete(job);
    }

    public List<JobResponseDTO> getJobsByEmployer(Employer employer) {
        List<Job> jobs = jobRepository.findByEmployer(employer);

        return jobs.stream()
                .map(this::mapToDTO)
                .toList();
    }

}
