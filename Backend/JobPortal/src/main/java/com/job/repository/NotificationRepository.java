package com.job.repository;

import com.job.entity.JobSeeker;
import com.job.entity.Notification;
import com.job.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(JobSeeker jobSeeker);
    void deleteAllByRecipient(User recipient);

    int countByRecipientAndSeenFalse(JobSeeker jobSeeker);
}
