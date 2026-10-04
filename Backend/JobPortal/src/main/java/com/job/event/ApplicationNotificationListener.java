package com.job.event;

import com.job.entity.Notification;
import com.job.enums.ApplicationStatus;
import com.job.repository.ApplicationRepository;
import com.job.repository.JobSeekerRepository;
import com.job.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Persists the in-app notification for a status change. A plain {@link EventListener} on purpose:
 * it runs synchronously inside the publisher's transaction, so the status change and its
 * notification commit or roll back together.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationNotificationListener {

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_COMPANY_LENGTH = 80;

    private final NotificationRepository notificationRepository;
    private final ApplicationRepository applicationRepository;
    private final JobSeekerRepository jobSeekerRepository;

    @EventListener
    public void onStatusChanged(ApplicationStatusChangedEvent event) {
        Notification notification = new Notification();
        notification.setMessage(buildMessage(event.jobTitle(), event.companyName(), event.newStatus()));
        notification.setRecipient(jobSeekerRepository.getReferenceById(event.jobSeekerId()));
        notification.setApplication(applicationRepository.getReferenceById(event.applicationId()));
        notificationRepository.save(notification);
        log.info("Notification created for job seeker id: {} regarding application id: {}",
                event.jobSeekerId(), event.applicationId());
    }

    static String buildMessage(String jobTitle, String companyName, ApplicationStatus status) {
        return String.format(
                "Update: Your application for '%s' at %s has been %s.",
                truncate(jobTitle, MAX_TITLE_LENGTH),
                truncate(companyName, MAX_COMPANY_LENGTH),
                status.displayLabel()
        );
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 1) + "…";
    }
}
