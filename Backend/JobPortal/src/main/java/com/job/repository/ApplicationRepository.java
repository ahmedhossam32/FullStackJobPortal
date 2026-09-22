package com.job.repository;

import com.job.entity.Application;
import com.job.entity.Job;
import com.job.entity.JobSeeker;
import com.job.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query(value = "SELECT a FROM Application a JOIN FETCH a.job j JOIN FETCH j.employer WHERE a.jobSeeker.id = :jobSeekerId",
           countQuery = "SELECT COUNT(a) FROM Application a WHERE a.jobSeeker.id = :jobSeekerId")
    Page<Application> findByJobSeekerId(@Param("jobSeekerId") Long jobSeekerId, Pageable pageable);

    boolean existsByJobAndJobSeeker(Job job, JobSeeker jobSeeker);
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);
    @Query("SELECT a FROM Application a JOIN FETCH a.jobSeeker JOIN FETCH a.job j JOIN FETCH j.employer WHERE a.job = :job")
    List<Application> findByJob(@Param("job") Job job);

    @Query("SELECT a FROM Application a JOIN FETCH a.jobSeeker JOIN FETCH a.job j JOIN FETCH j.employer WHERE j.employer.id = :employerId")
    List<Application> findByJob_EmployerId(@Param("employerId") Long employerId);
    List<Application> findByJob_EmployerIdAndStatus(Long employerId, ApplicationStatus status);
}
