package com.job.repository;

import com.job.entity.Application;
import com.job.entity.Employer;
import com.job.entity.Job;
import com.job.entity.JobSeeker;
import com.job.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ApplicationRepository extends JpaRepository<Application,Long>
{
    List<Application> findByJobSeeker(JobSeeker jobSeeker);
    boolean existsByJobAndJobSeeker(Job job, JobSeeker jobSeeker);
    List<Application> findByJob(Job job);
    List<Application> findByJob_Employer(Employer employer);
    List<Application> findByJob_EmployerAndStatus(Employer employer, ApplicationStatus status);

}
