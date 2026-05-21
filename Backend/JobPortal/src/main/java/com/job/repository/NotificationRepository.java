package com.job.repository;

import com.job.entity.JobSeeker;
import com.job.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(JobSeeker jobSeeker);
    void deleteAllByRecipient(JobSeeker recipient);
    int countByRecipientAndSeenFalse(JobSeeker jobSeeker);
    List<Notification> findByRecipientAndSeenFalseOrderByCreatedAtDesc(JobSeeker jobSeeker);
}
