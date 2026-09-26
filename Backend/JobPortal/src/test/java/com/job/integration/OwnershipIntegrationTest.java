package com.job.integration;

import com.job.entity.Application;
import com.job.entity.Notification;
import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A valid token for the wrong owner must still be rejected -- role checks alone aren't enough,
 * the service layer's per-resource ownership checks (mapped to 403 via ForbiddenException)
 * have to actually run.
 */
class OwnershipIntegrationTest extends AbstractIntegrationTest {

    @Test
    void secondEmployerCannotUpdateDeleteOrListApplicantsOfFirstEmployersJob() throws Exception {
        AuthedUser employer1 = createEmployer("ownemp1");
        AuthedUser employer2 = createEmployer("ownemp2");
        long jobId = createJob(employer1.token(), "Ownership Test Job");

        mockMvc.perform(put("/jobs/{id}", jobId)
                        .header(AUTH_HEADER, bearer(employer2.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson("Hijacked title")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        mockMvc.perform(delete("/jobs/{id}", jobId)
                        .header(AUTH_HEADER, bearer(employer2.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        mockMvc.perform(get("/applications/job/{jobId}", jobId)
                        .header(AUTH_HEADER, bearer(employer2.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        // The rightful owner is unaffected.
        mockMvc.perform(get("/applications/job/{jobId}", jobId)
                        .header(AUTH_HEADER, bearer(employer1.token())))
                .andExpect(status().isOk());
    }

    @Test
    void secondSeekerCannotReadOrWithdrawFirstSeekersApplication() throws Exception {
        AuthedUser employer = createEmployer("ownappemp");
        AuthedUser seeker1 = createJobSeeker("ownappsk1");
        AuthedUser seeker2 = createJobSeeker("ownappsk2");

        giveResume(seeker1.id(), "https://res.cloudinary.test/raw/upload/seeker1-resume.pdf");
        long jobId = createJob(employer.token(), "Ownership App Job");
        long applicationId = applyToJob(seeker1.token(), jobId);

        mockMvc.perform(get("/applications/{id}", applicationId)
                        .header(AUTH_HEADER, bearer(seeker2.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        mockMvc.perform(delete("/applications/{id}", applicationId)
                        .header(AUTH_HEADER, bearer(seeker2.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        // The application must still exist -- seeker2's withdraw attempt must not have succeeded.
        mockMvc.perform(get("/applications/{id}", applicationId)
                        .header(AUTH_HEADER, bearer(seeker1.token())))
                .andExpect(status().isOk());
    }

    @Test
    void secondSeekerCannotReadOrMarkFirstSeekersNotification() throws Exception {
        AuthedUser employer = createEmployer("ownnotifyemp");
        AuthedUser seeker1 = createJobSeeker("ownnotifysk1");
        AuthedUser seeker2 = createJobSeeker("ownnotifysk2");

        giveResume(seeker1.id(), "https://res.cloudinary.test/raw/upload/seeker1-resume.pdf");
        long jobId = createJob(employer.token(), "Ownership Notification Job");
        long applicationId = applyToJob(seeker1.token(), jobId);

        mockMvc.perform(put("/applications/{id}/status", applicationId)
                        .header(AUTH_HEADER, bearer(employer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REVIEWED\"}"))
                .andExpect(status().isOk());

        Notification notification = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(seeker1.id())
                .stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected a notification to have been created"));

        mockMvc.perform(put("/notifications/{id}/read", notification.getId())
                        .header(AUTH_HEADER, bearer(seeker2.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        // seeker2 has no visibility endpoint for someone else's single notification besides
        // mark-as-read, but confirm the list of unread notifications for seeker1 is untouched.
        mockMvc.perform(get("/notifications/unread-count")
                        .header(AUTH_HEADER, bearer(seeker1.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }
}
