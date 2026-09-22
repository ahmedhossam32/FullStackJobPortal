package com.job.service.interfaces;

import com.job.dto.response.NotificationDTO;
import com.job.entity.Notification;

import java.util.List;

public interface INotificationService {
    List<NotificationDTO> getNotificationsFor(Long jobSeekerId);
    NotificationDTO mapToDTO(Notification notification);
    void deleteAllNotificationsForUser(Long jobSeekerId);
    void markAsRead(Long notificationId, Long jobSeekerId);
    int getUnreadCount(Long jobSeekerId);
    List<NotificationDTO> getUnreadNotifications(Long jobSeekerId);
}
