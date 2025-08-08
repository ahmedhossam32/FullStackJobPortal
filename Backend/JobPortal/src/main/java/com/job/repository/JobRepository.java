package com.job.repository;

import com.job.entity.Employer;
import com.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job,Long>
{

    List<Job> findByEmployer(Employer employer);
}