package com.job.designpatterns.Observer;

import com.job.enums.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plain unit test (no Spring context, no DB) for the notification message builder.
 * Reaching JobSeekerNotificationObserver.notify(...) live requires a job seeker with an
 * uploaded resume, which requires a real Cloudinary upload -- out of bounds for this task's
 * live verification, so the overflow fix (notification.message is varchar(255)) is locked
 * down here instead.
 */
class JobSeekerNotificationObserverTest {

    @Test
    void shortInputsProduceExactMessageWithNoTruncation() {
        String message = JobSeekerNotificationObserver.buildMessage("Backend Engineer", "Acme", ApplicationStatus.OFFERED);

        assertEquals("Update: Your application for 'Backend Engineer' at Acme has been Offered.", message);
    }

    @ParameterizedTest
    @EnumSource(ApplicationStatus.class)
    void messageFitsInVarchar255ForMaximumLengthInputs(ApplicationStatus status) {
        String maxDtoTitle = "T".repeat(200); // JobRequestDTO caps title at 200 chars
        String unboundedCompanyName = "C".repeat(1000); // companyName has no @Size limit today

        String message = JobSeekerNotificationObserver.buildMessage(maxDtoTitle, unboundedCompanyName, status);

        assertTrue(message.length() <= 255,
                "message length " + message.length() + " exceeds notification.message varchar(255) for status " + status);
    }

    @Test
    void longTitleAndCompanyAreTruncatedWithEllipsis() {
        String longTitle = "T".repeat(150);
        String longCompany = "C".repeat(150);

        String message = JobSeekerNotificationObserver.buildMessage(longTitle, longCompany, ApplicationStatus.REJECTED);

        assertTrue(message.contains("…"), "expected the truncated title/company to end with an ellipsis");
        assertTrue(message.length() <= 255);
    }
}
