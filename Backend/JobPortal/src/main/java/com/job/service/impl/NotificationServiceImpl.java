package com.job.service.impl;

import com.job.dto.response.NotificationDTO;
import com.job.entity.JobSeeker;
import com.job.entity.Notification;
import com.job.exception.ResourceNotFoundException;
import com.job.exception.UnauthorizedException;
import com.job.repository.NotificationRepository;
import com.job.service.interfaces.INotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationDTO> getNotificationsFor(JobSeeker jobSeeker) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(jobSeeker).stream()
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

        if (notification.getApplication() != null &&
                notification.getApplication().getJob() != null &&
                notification.getApplication().getJob().getEmployer() != null) {

            String logoFileName = notification.getApplication().getJob().getEmployer().getProfilePictureFileName();
            if (logoFileName != null) {
                dto.setCompanyLogoUrl(logoFileName);
            }
        }

        return dto;
    }

    @Override
    @Transactional
    public void deleteAllNotificationsForUser(JobSeeker jobSeeker) {
        notificationRepository.deleteAllByRecipient(jobSeeker);
    }

    @Override
    public void markAsRead(Long notificationId, JobSeeker jobSeeker) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(jobSeeker.getId())) {
            throw new UnauthorizedException("You are not authorized to mark this notification as read");
        }

        notification.setSeen(true);
        notificationRepository.save(notification);
    }

    @Override
    public int getUnreadCount(JobSeeker jobSeeker) {
        return notificationRepository.countByRecipientAndSeenFalse(jobSeeker);
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(JobSeeker jobSeeker) {
        return notificationRepository.findByRecipientAndSeenFalseOrderByCreatedAtDesc(jobSeeker).stream()
                .map(this::mapToDTO)
                .toList();
    }
}
