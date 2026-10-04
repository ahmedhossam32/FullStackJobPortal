package com.job.integration;

import com.job.integration.support.AbstractIntegrationTest;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Locks down D10: the Cloudinary round-trip must run with no transaction open and no pooled DB
 * connection held, and only the URL write afterwards touches the database. The Cloudinary mock
 * samples the transaction state and the Hikari pool's active-connection count at the moment the
 * upload would be in flight.
 */
class UploadTransactionBoundaryIntegrationTest extends AbstractIntegrationTest {

    private static final String RESUME_URL = "https://res.cloudinary.test/raw/upload/d10-resume.pdf";
    private static final String IMAGE_URL = "https://res.cloudinary.test/image/upload/d10-image.png";

    @Autowired private DataSource dataSource;

    @Test
    void resumeUploadHoldsNoTransactionOrConnectionDuringCloudinaryCall() throws Exception {
        AuthedUser seeker = createJobSeeker("d10resume");
        AtomicBoolean transactionActive = new AtomicBoolean(true);
        AtomicInteger activeConnections = new AtomicInteger(-1);
        when(cloudinaryService.uploadResume(any())).thenAnswer(invocation -> {
            transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            activeConnections.set(activeConnections());
            return RESUME_URL;
        });

        MockMultipartFile pdf = new MockMultipartFile("file", "resume.pdf", "application/pdf", VALID_PDF_BYTES);
        mockMvc.perform(multipart("/user/jobseeker/upload-resume").file(pdf)
                        .header(AUTH_HEADER, bearer(seeker.token())))
                .andExpect(status().isOk());

        assertFalse(transactionActive.get(), "a transaction was open during the Cloudinary upload");
        assertEquals(0, activeConnections.get(), "a pooled DB connection was held during the Cloudinary upload");
        assertEquals(RESUME_URL, jobSeekerRepository.findById(seeker.id()).orElseThrow().getResumeUrl());
    }

    @Test
    void profilePictureUploadHoldsNoTransactionOrConnectionDuringCloudinaryCall() throws Exception {
        AuthedUser seeker = createJobSeeker("d10image");
        AtomicBoolean transactionActive = new AtomicBoolean(true);
        AtomicInteger activeConnections = new AtomicInteger(-1);
        when(cloudinaryService.uploadImage(any())).thenAnswer(invocation -> {
            transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            activeConnections.set(activeConnections());
            return IMAGE_URL;
        });

        MockMultipartFile png = new MockMultipartFile("file", "avatar.png", "image/png", VALID_PNG_BYTES);
        mockMvc.perform(multipart("/user/upload-profile-picture").file(png)
                        .header(AUTH_HEADER, bearer(seeker.token())))
                .andExpect(status().isOk());

        assertFalse(transactionActive.get(), "a transaction was open during the Cloudinary upload");
        assertEquals(0, activeConnections.get(), "a pooled DB connection was held during the Cloudinary upload");
        assertEquals(IMAGE_URL, userRepository.findById(seeker.id()).orElseThrow().getProfilePictureUrl());
    }

    private int activeConnections() throws Exception {
        return dataSource.unwrap(HikariDataSource.class).getHikariPoolMXBean().getActiveConnections();
    }
}
