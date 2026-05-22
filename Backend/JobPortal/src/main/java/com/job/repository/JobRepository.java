package com.job.repository;

import com.job.entity.Employer;
import com.job.entity.Job;
import com.job.enums.JobType;
import com.job.enums.WorkMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByEmployer(Employer employer);

    Page<Job> findAll(Pageable pageable);
    Page<Job> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Job> findByType(JobType type, Pageable pageable);
    Page<Job> findByLocationContainingIgnoreCase(String location, Pageable pageable);
    Page<Job> findByWorkMode(WorkMode workMode, Pageable pageable);
}
