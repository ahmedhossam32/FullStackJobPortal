package com.job.service.impl;

import com.job.dto.response.NotificationDTO;
import com.job.entity.Application;
import com.job.entity.Notification;
import com.job.exception.ForbiddenException;
import com.job.exception.ResourceNotFoundException;
import com.job.repository.NotificationRepository;
import com.job.service.NotificationService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsFor(Long jobSeekerId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(jobSeekerId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setSeen(notification.isSeen());

        if (notification.getApplication() != null) {
            dto.setApplicationId(notification.getApplication().getId());

            Application app = notification.getApplication();
            if (app.getJob() != null && app.getJob().getEmployer() != null) {
                String logoUrl = app.getJob().getEmployer().getProfilePictureUrl();
                if (logoUrl != null) {
                    dto.setCompanyLogoUrl(logoUrl);
                }
            }
        }

        return dto;
    }

    @Override
    @Transactional
    public void deleteAllNotificationsForUser(Long jobSeekerId) {
        notificationRepository.deleteAllByRecipientId(jobSeekerId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long jobSeekerId) {
        log.info("Marking notification id: {} as read for user id: {}", notificationId, jobSeekerId);
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
                .map(this::mapToDTO)
                .toList();
    }
}
