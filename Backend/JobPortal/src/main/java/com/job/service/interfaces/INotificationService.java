package com.job.service.interfaces;

import com.job.dto.response.NotificationDTO;
import com.job.entity.JobSeeker;
import com.job.entity.Notification;
import com.job.entity.User;

import java.util.List;

public interface INotificationService {
    List<NotificationDTO> getNotificationsFor(JobSeeker jobSeeker);
    NotificationDTO mapToDTO(Notification notification);
    void deleteAllNotificationsForUser(User user);
}