package com.job.integration;

import com.job.enums.ApplicationStatus;
import com.job.event.ApplicationStatusChangedEvent;
import com.job.event.ApplicationSubmittedEvent;
import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Locks down C10: application emails go out only after the transaction that published the event
 * commits. Each test publishes the event directly inside a {@link TransactionTemplate} so the
 * transaction outcome is under the test's control. Every test uses a unique recipient address,
 * so an async send left over from another test can't satisfy or break these verifications.
 */
class EmailAfterCommitIntegrationTest extends AbstractIntegrationTest {

    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    void statusEmailIsSentWhenTheTransactionCommits() throws Exception {
        String recipient = uniqueEmail();
        ApplicationStatusChangedEvent event = statusChangedEventFor(recipient);

        new TransactionTemplate(transactionManager).executeWithoutResult(tx -> eventPublisher.publishEvent(event));

        // Positive control: proves the listener and the verification below can observe a send,
        // so the never() in the rollback test is meaningful.
        verify(emailService, timeout(2000)).sendApplicationStatusUpdate(
                eq(recipient), anyString(), anyString(), eq("OFFERED"));
    }

    @Test
    void statusEmailIsNotSentWhenTheTransactionRollsBack() throws Exception {
        String recipient = uniqueEmail();
        ApplicationStatusChangedEvent event = statusChangedEventFor(recipient);
        assertEquals(0, notificationRepository.findByRecipientIdOrderByCreatedAtDesc(event.jobSeekerId()).size());

        new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
            eventPublisher.publishEvent(event);
            tx.setRollbackOnly();
        });

        verify(emailService, after(500).never()).sendApplicationStatusUpdate(
                eq(recipient), anyString(), anyString(), anyString());
        // The same-transaction notification write rolled back with it.
        assertEquals(0, notificationRepository.findByRecipientIdOrderByCreatedAtDesc(event.jobSeekerId()).size());
    }

    @Test
    void confirmationEmailIsNotSentWhenTheTransactionRollsBack() {
        String recipient = uniqueEmail();

        new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
            eventPublisher.publishEvent(new ApplicationSubmittedEvent(recipient, "Seeker", "Job", "Company"));
            tx.setRollbackOnly();
        });

        verify(emailService, after(500).never()).sendApplicationConfirmation(
                eq(recipient), anyString(), anyString(), anyString());
    }

    @Test
    void eventPublishedOutsideATransactionIsDroppedNotEmailed() {
        String recipient = uniqueEmail();

        eventPublisher.publishEvent(new ApplicationSubmittedEvent(recipient, "Seeker", "Job", "Company"));

        verify(emailService, after(500).never()).sendApplicationConfirmation(
                eq(recipient), anyString(), anyString(), anyString());
    }

    private static String uniqueEmail() {
        return "c10-" + UUID.randomUUID() + "@example.com";
    }

    // ApplicationStatusChangedEvent also drives the same-transaction notification listener, which
    // inserts a Notification pointing at these IDs, so they must reference a real application.
    private ApplicationStatusChangedEvent statusChangedEventFor(String recipient) throws Exception {
        AuthedUser employer = createEmployer("c10emp");
        AuthedUser seeker = createJobSeeker("c10sk");
        giveResume(seeker.id(), "https://res.cloudinary.test/raw/upload/resume.pdf");
        long applicationId = applyToJob(seeker.token(), createJob(employer.token(), "C10 Job"));
        return new ApplicationStatusChangedEvent(
                applicationId, seeker.id(), recipient, "Seeker", "C10 Job", "Company", ApplicationStatus.OFFERED);
    }
}
