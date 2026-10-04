package com.job.integration;

import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void applyingWithoutAResumeReturns400() throws Exception {
        AuthedUser employer = createEmployer("appnoresumeemp");
        AuthedUser seeker = createJobSeeker("appnoresumesk");
        long jobId = createJob(employer.token(), "No Resume Job");

        mockMvc.perform(post("/applications")
                        .header(AUTH_HEADER, bearer(seeker.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobId\":" + jobId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void applyingTwiceReturns409() throws Exception {
        AuthedUser employer = createEmployer("appdupemp");
        AuthedUser seeker = createJobSeeker("appdupsk");
        giveResume(seeker.id(), "https://res.cloudinary.test/raw/upload/resume.pdf");
        long jobId = createJob(employer.token(), "Duplicate Application Job");

        applyToJob(seeker.token(), jobId);

        mockMvc.perform(post("/applications")
                        .header(AUTH_HEADER, bearer(seeker.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobId\":" + jobId + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void statusUpdateCreatesNotificationWithMessageUnder255CharsForLongestAllowedInputs() throws Exception {
        AuthedUser employer = createEmployer("appnotifyemp");
        AuthedUser seeker = createJobSeeker("appnotifysk");
        giveResume(seeker.id(), "https://res.cloudinary.test/raw/upload/resume.pdf");

        String maxTitle = "T".repeat(200); // JobRequestDTO caps title at 200 chars
        long jobId = createJob(employer.token(), maxTitle);
        long applicationId = applyToJob(seeker.token(), jobId);

        mockMvc.perform(put("/applications/{id}/status", applicationId)
                        .header(AUTH_HEADER, bearer(employer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFERED\"}"))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/notifications")
                        .header(AUTH_HEADER, bearer(seeker.token())))
                .andExpect(status().isOk())
                .andReturn();
        String message = objectMapper.readTree(result.getResponse().getContentAsString()).get(0).get("message").asText();
        assertFalse(message.isBlank());
        assertTrue(message.length() <= 255,
                "notification message length " + message.length() + " exceeds varchar(255)");
    }

    @Test
    void seekerCanListNotificationAndUnreadCountFollows() throws Exception {
        AuthedUser employer = createEmployer("appnotifylistemp");
        AuthedUser seeker = createJobSeeker("appnotifylistsk");
        giveResume(seeker.id(), "https://res.cloudinary.test/raw/upload/resume.pdf");
        long jobId = createJob(employer.token(), "Notification List Job");
        long applicationId = applyToJob(seeker.token(), jobId);

        mockMvc.perform(get("/notifications/unread-count")
                        .header(AUTH_HEADER, bearer(seeker.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));

        mockMvc.perform(put("/applications/{id}/status", applicationId)
                        .header(AUTH_HEADER, bearer(employer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REVIEWED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notifications")
                        .header(AUTH_HEADER, bearer(seeker.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].applicationId").value(applicationId))
                .andExpect(jsonPath("$[0].seen").value(false));

        mockMvc.perform(get("/notifications/unread-count")
                        .header(AUTH_HEADER, bearer(seeker.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));

        long notificationId = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(seeker.id())
                .get(0).getId();

        mockMvc.perform(put("/notifications/{id}/read", notificationId)
                        .header(AUTH_HEADER, bearer(seeker.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notifications/unread-count")
                        .header(AUTH_HEADER, bearer(seeker.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
    }

    @Test
    void applyingEventuallySendsConfirmationEmail() throws Exception {
        AuthedUser employer = createEmployer("appmailemp");
        AuthedUser seeker = createJobSeeker("appmailsk");
        giveResume(seeker.id(), "https://res.cloudinary.test/raw/upload/resume.pdf");
        long jobId = createJob(employer.token(), "Confirmation Email Job");

        applyToJob(seeker.token(), jobId);

        // Sent from an @Async after-commit listener, so wait for it instead of asserting immediately.
        verify(emailService, timeout(2000)).sendApplicationConfirmation(
                seeker.username() + "@example.com", "Test Seeker appmailsk", "Confirmation Email Job", "Acme appmailemp");
    }

    @Test
    void statusUpdateEventuallySendsStatusEmail() throws Exception {
        AuthedUser employer = createEmployer("appstatusmailemp");
        AuthedUser seeker = createJobSeeker("appstatusmailsk");
        giveResume(seeker.id(), "https://res.cloudinary.test/raw/upload/resume.pdf");
        long jobId = createJob(employer.token(), "Status Email Job");
        long applicationId = applyToJob(seeker.token(), jobId);

        mockMvc.perform(put("/applications/{id}/status", applicationId)
                        .header(AUTH_HEADER, bearer(employer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFERED\"}"))
                .andExpect(status().isOk());

        verify(emailService, timeout(2000)).sendApplicationStatusUpdate(
                seeker.username() + "@example.com", "Test Seeker appstatusmailsk", "Status Email Job", "OFFERED");
    }
}
