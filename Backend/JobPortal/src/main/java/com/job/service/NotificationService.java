package com.job.service;

import com.job.dto.response.NotificationDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getNotificationsFor(Long jobSeekerId);
    void deleteAllNotificationsForUser(Long jobSeekerId);
    void markAsRead(Long notificationId, Long jobSeekerId);
    int getUnreadCount(Long jobSeekerId);
    List<NotificationDTO> getUnreadNotifications(Long jobSeekerId);
}
