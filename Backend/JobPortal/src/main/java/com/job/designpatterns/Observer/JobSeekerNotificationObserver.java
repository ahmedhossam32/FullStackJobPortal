package com.job.designpatterns.Observer;

import com.job.entity.Application;
import com.job.entity.JobSeeker;
import com.job.entity.Notification;
import com.job.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobSeekerNotificationObserver implements ApplicationObserver {

    private final NotificationRepository notificationRepository;

    @Async
    @Override
    public void notify(JobSeeker jobSeeker, Application application) {
        String message = String.format(
                "Update: Your application for '%s' at %s has been %s.",
                application.getJob().getTitle(),
                application.getJob().getEmployer().getCompanyName(),
                application.getStatus().name().substring(0, 1).toUpperCase() + application.getStatus().name().substring(1).toLowerCase()
        );

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setRecipient(jobSeeker);
        notification.setApplication(application);
        notificationRepository.save(notification);
    }
}
