package com.job.event;

import com.job.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends outbound emails only once the change that triggered them has committed, then hands the
 * send off to the async executor so SMTP latency never blocks the request.
 *
 * <p>{@code fallbackExecution} is deliberately left at {@code false}: both publish sites are
 * {@code @Transactional}, and an event published outside a transaction is dropped rather than
 * emailed immediately, since there would be no commit to wait for.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEmailListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        log.info("Sending application confirmation email for job: '{}'", event.jobTitle());
        emailService.sendApplicationConfirmation(
                event.seekerEmail(),
                event.seekerName(),
                event.jobTitle(),
                event.companyName()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(ApplicationStatusChangedEvent event) {
        log.info("Sending status update email for application id: {}", event.applicationId());
        emailService.sendApplicationStatusUpdate(
                event.seekerEmail(),
                event.seekerName(),
                event.jobTitle(),
                event.newStatus().toString()
        );
    }
}
