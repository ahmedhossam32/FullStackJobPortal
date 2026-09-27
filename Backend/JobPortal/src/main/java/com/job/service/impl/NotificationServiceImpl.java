package com.job.service.impl;

import com.job.dto.response.NotificationDTO;
import com.job.entity.Notification;
import com.job.exception.ForbiddenException;
import com.job.exception.ResourceNotFoundException;
import com.job.mapper.NotificationMapper;
import com.job.repository.NotificationRepository;
import com.job.service.NotificationService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsFor(Long jobSeekerId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(jobSeekerId).stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAllNotificationsForUser(Long jobSeekerId) {
        notificationRepository.deleteAllByRecipientId(jobSeekerId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long jobSeekerId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(jobSeekerId)) {
            throw new ForbiddenException("You are not authorized to mark this notification as read");
        }

        notification.setSeen(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(Long jobSeekerId) {
        return notificationRepository.countByRecipientIdAndSeenFalse(jobSeekerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long jobSeekerId) {
        return notificationRepository.findByRecipientIdAndSeenFalseOrderByCreatedAtDesc(jobSeekerId).stream()
                .map(notificationMapper::toDTO)
                .toList();
    }
}
