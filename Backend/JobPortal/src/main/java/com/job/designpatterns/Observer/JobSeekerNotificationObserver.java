package com.job.designpatterns.Observer;

import com.job.entity.Application;
import com.job.entity.JobSeeker;
import com.job.entity.Notification;
import com.job.enums.ApplicationStatus;
import com.job.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobSeekerNotificationObserver implements ApplicationObserver {

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_COMPANY_LENGTH = 80;

    private final NotificationRepository notificationRepository;

    @Override
    public void notify(JobSeeker jobSeeker, Application application) {
        String message = buildMessage(
                application.getJob().getTitle(),
                application.getJob().getEmployer().getCompanyName(),
                application.getStatus()
        );

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setRecipient(jobSeeker);
        notification.setApplication(application);
        notificationRepository.save(notification);
        log.info("Notification created for user: {} regarding application for job: '{}'",
                jobSeeker.getUsername(), application.getJob().getTitle());
    }

    static String buildMessage(String jobTitle, String companyName, ApplicationStatus status) {
        String statusLabel = status.name().substring(0, 1).toUpperCase() + status.name().substring(1).toLowerCase();
        return String.format(
                "Update: Your application for '%s' at %s has been %s.",
                truncate(jobTitle, MAX_TITLE_LENGTH),
                truncate(companyName, MAX_COMPANY_LENGTH),
                statusLabel
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
