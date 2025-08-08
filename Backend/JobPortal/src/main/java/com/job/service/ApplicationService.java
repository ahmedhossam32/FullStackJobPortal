package com.job.service;

import com.job.designpatterns.Observer.ApplicationObserver;
import com.job.dto.ApplicationRequestDTO;
import com.job.dto.ApplicationResponseDTO;
import com.job.dto.ApplicationViewForEmployerDTO;
import com.job.entity.Application;
import com.job.entity.Employer;
import com.job.entity.Job;
import com.job.entity.JobSeeker;
import com.job.enums.ApplicationStatus;
import com.job.repository.ApplicationRepository;
import com.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final List<ApplicationObserver> observers;


    public ApplicationResponseDTO applyToJob(ApplicationRequestDTO dto, JobSeeker jobSeeker) {
        if (jobSeeker.getResumeFileName() == null || jobSeeker.getResumeFileName().isBlank()) {
            throw new RuntimeException("You must upload a resume before applying to a job.");
        }

        Job job = jobRepository.findById(dto.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        boolean alreadyApplied = applicationRepository.existsByJobAndJobSeeker(job, jobSeeker);
        if (alreadyApplied) {
            throw new RuntimeException("You have already applied for this job");
        }

        List<String> screeningQs = job.getScreeningQuestions();
        if (screeningQs != null && !screeningQs.isEmpty()) {
            if (dto.getScreeningAnswers() == null || dto.getScreeningAnswers().size() != screeningQs.size()) {
                throw new RuntimeException("You must answer all required screening questions.");
            }
        }

        Application application = new Application();
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setResumeUrl(dto.getResumeUrl());
        application.setStatus(ApplicationStatus.PENDING);
        application.setAppliedAt(LocalDateTime.now());

        // TODO: Save screening answers to DB if needed in the future

        applicationRepository.save(application);

        return mapToDTO(application);
    }

    public List<ApplicationViewForEmployerDTO> getAllApplicationsForEmployer(Employer employer, ApplicationStatus status) {
        List<Application> applications;

        if (status == null) {
            applications = applicationRepository.findByJob_Employer(employer);
        } else {
            applications = applicationRepository.findByJob_EmployerAndStatus(employer, status);
        }

        return applications.stream()
                .map(this::mapToEmployerDTO)
                .toList();
    }


    private ApplicationResponseDTO mapToDTO(Application application) {
        ApplicationResponseDTO dto = new ApplicationResponseDTO();

        dto.setApplicationId(application.getId());
        dto.setUsername(application.getJobSeeker().getUsername());
        dto.setStatus(application.getStatus());
        dto.setAppliedAt(application.getAppliedAt());


        Job job = application.getJob();
        dto.setJobTitle(job.getTitle());
        dto.setJobType(job.getType().toString());           // Assuming JobType is an enum
        dto.setWorkMode(job.getWorkMode().toString());      // Assuming WorkMode is an enum
        dto.setLocation(job.getLocation());
        dto.setJobDescription(job.getDescription());


        Employer employer = job.getEmployer();
        dto.setCompanyName(employer.getCompanyName());
        dto.setCompanyLogoUrl(employer.getProfilePictureFileName()); // Assuming logo stored as profile picture field

        dto.setResumeUrl(application.getResumeUrl());

        return dto;
    }


    public List<ApplicationResponseDTO> getMyApplications(JobSeeker jobSeeker) {
        return applicationRepository.findByJobSeeker(jobSeeker).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public ApplicationResponseDTO getApplicationById(Long id, JobSeeker requester) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getJobSeeker().getId().equals(requester.getId())) {
            throw new RuntimeException("Unauthorized access to application");
        }

        return mapToDTO(app);
    }

    public void withdrawApplication(Long id, JobSeeker requester) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getJobSeeker().getId().equals(requester.getId())) {
            throw new RuntimeException("Unauthorized to withdraw this application");
        }

        applicationRepository.delete(app);
    }

    public List<ApplicationViewForEmployerDTO> getApplicationsForJob(Long jobId, Employer employer) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized to view applications for this job");
        }

        List<Application> applications = applicationRepository.findByJob(job);

        return applications.stream()
                .map(this::mapToEmployerDTO)
                .toList();
    }

    public ApplicationViewForEmployerDTO getApplicationViewForEmployer(Long id, Employer employer) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getJob().getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized to view this application");
        }

        return mapToEmployerDTO(app);
    }

    private ApplicationViewForEmployerDTO mapToEmployerDTO(Application app) {
        JobSeeker applicant = app.getJobSeeker();
        Job job = app.getJob();

        ApplicationViewForEmployerDTO dto = new ApplicationViewForEmployerDTO();
        dto.setApplicantUsername(applicant.getUsername());
        dto.setApplicantDOB(applicant.getDob());
        dto.setApplicantEmail(applicant.getEmail());
        dto.setId(app.getId());
        dto.setResumeUrl(app.getResumeUrl());
        dto.setApplicantName(applicant.getName());
        dto.setApplicantProfilePicture(applicant.getProfilePictureFileName());
        dto.setStatus(app.getStatus());
        dto.setAppliedAt(app.getAppliedAt());

        dto.setJobTitle(job.getTitle());

        return dto;
    }


    public void updateApplicationStatus(Long applicationId, ApplicationStatus newStatus, Employer employer) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getJob().getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized to update this application");
        }

        app.setStatus(newStatus);
        applicationRepository.save(app);
        notifyObservers(app.getJobSeeker(), app);
}

private void notifyObservers(JobSeeker jobSeeker, Application application) {
    for (ApplicationObserver observer : observers) {
        observer.notify(jobSeeker, application);
    }
}

    public boolean hasUserAppliedToJob(Long jobId, JobSeeker jobSeeker) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        return applicationRepository.existsByJobAndJobSeeker(job, jobSeeker);
    }

}
