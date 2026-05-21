package com.job.service.impl;

import com.job.dto.response.NotificationDTO;
import com.job.entity.JobSeeker;
import com.job.entity.Notification;
import com.job.entity.User;
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
                dto.setCompanyLogoUrl("http://localhost:8080/files/profile-picture/" + logoFileName);
            }
        }

        return dto;
    }

    @Override
    @Transactional
    public void deleteAllNotificationsForUser(User user) {
        notificationRepository.deleteAllByRecipient(user);
    }
}