package com.job.integration;

import com.job.exception.FileStorageException;
import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Upload validation with Cloudinary mocked out -- FileValidator's real byte-level checks run. */
class UploadsIntegrationTest extends AbstractIntegrationTest {

    private AuthedUser seeker;

    private AuthedUser seeker() throws Exception {
        if (seeker == null) {
            seeker = createJobSeeker("upl");
        }
        return seeker;
    }

    // ── Profile picture (image) ─────────────────────────────────────────────

    @Test
    void svgLabeledAsImageReturns415AndNeverCallsCloudinary() throws Exception {
        AuthedUser user = seeker();
        MockMultipartFile svg = new MockMultipartFile("file", "evil.svg", "image/svg+xml",
                "<svg onload=\"x()\"><script>alert(1)</script></svg>".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/user/upload-profile-picture").file(svg)
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));

        verify(cloudinaryService, never()).uploadImage(any());
    }

    @Test
    void textFileLabeledImagePngReturns415AndNeverCallsCloudinary() throws Exception {
        AuthedUser user = seeker();
        MockMultipartFile textAsImage = new MockMultipartFile("file", "note.txt", "image/png",
                "just plain text, not an image".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/user/upload-profile-picture").file(textAsImage)
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));

        verify(cloudinaryService, never()).uploadImage(any());
    }

    @Test
    void pdfBytesLabeledAsImageReturns415AndNeverCallsCloudinary() throws Exception {
        AuthedUser user = seeker();
        MockMultipartFile pdfAsImage = new MockMultipartFile("file", "resume.pdf", "image/png", VALID_PDF_BYTES);

        mockMvc.perform(multipart("/user/upload-profile-picture").file(pdfAsImage)
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));

        verify(cloudinaryService, never()).uploadImage(any());
    }

    @Test
    void emptyImageReturns400AndNeverCallsCloudinary() throws Exception {
        AuthedUser user = seeker();
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/user/upload-profile-picture").file(empty)
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));

        verify(cloudinaryService, never()).uploadImage(any());
    }

    @Test
    void imageOverSizeLimitReturns413AndNeverCallsCloudinary() throws Exception {
        AuthedUser user = seeker();
        byte[] tooBig = new byte[6 * 1024 * 1024]; // over both the servlet limit and FileValidator's 5MB cap
        System.arraycopy(VALID_PNG_BYTES, 0, tooBig, 0, VALID_PNG_BYTES.length);
        MockMultipartFile big = new MockMultipartFile("file", "big.png", "image/png", tooBig);

        mockMvc.perform(multipart("/user/upload-profile-picture").file(big)
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error").value("PAYLOAD_TOO_LARGE"));

        verify(cloudinaryService, never()).uploadImage(any());
    }

    @Test
    void validPngReturns200AndCallsCloudinaryOnce() throws Exception {
        AuthedUser user = seeker();

        mockMvc.perform(multipart("/user/upload-profile-picture").file(pngPart())
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Profile picture uploaded successfully: https://res.cloudinary.test/image/upload/fake-image.png"));

        verify(cloudinaryService, times(1)).uploadImage(any());
    }

    // ── Resume (document) ────────────────────────────────────────────────────

    @Test
    void zipDocxBytesLabeledAsPdfReturns415AndNeverCallsCloudinary() throws Exception {
        AuthedUser user = seeker();
        byte[] zipHeader = {0x50, 0x4B, 0x03, 0x04, 0x14, 0x00};
        MockMultipartFile zipAsPdf = new MockMultipartFile("file", "resume.pdf", "application/pdf", zipHeader);

        mockMvc.perform(multipart("/user/jobseeker/upload-resume").file(zipAsPdf)
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));

        verify(cloudinaryService, never()).uploadResume(any());
    }

    @Test
    void emptyResumeReturns400AndNeverCallsCloudinary() throws Exception {
        AuthedUser user = seeker();
        MockMultipartFile empty = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/user/jobseeker/upload-resume").file(empty)
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));

        verify(cloudinaryService, never()).uploadResume(any());
    }

    @Test
    void resumeOverSizeLimitReturns413AndNeverCallsCloudinary() throws Exception {
        AuthedUser user = seeker();
        byte[] tooBig = new byte[6 * 1024 * 1024];
        System.arraycopy(VALID_PDF_BYTES, 0, tooBig, 0, VALID_PDF_BYTES.length);
        MockMultipartFile big = new MockMultipartFile("file", "big.pdf", "application/pdf", tooBig);

        mockMvc.perform(multipart("/user/jobseeker/upload-resume").file(big)
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error").value("PAYLOAD_TOO_LARGE"));

        verify(cloudinaryService, never()).uploadResume(any());
    }

    @Test
    void validPdfReturns200AndCallsCloudinaryOnce() throws Exception {
        AuthedUser user = seeker();

        mockMvc.perform(multipart("/user/jobseeker/upload-resume").file(pdfPart())
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Resume uploaded successfully: https://res.cloudinary.test/raw/upload/fake-resume.pdf"));

        verify(cloudinaryService, times(1)).uploadResume(any());
    }

    // ── Storage provider failure ─────────────────────────────────────────────

    @Test
    void cloudinaryFailureYields502WithGenericMessage() throws Exception {
        AuthedUser user = seeker();
        when(cloudinaryService.uploadImage(any()))
                .thenThrow(new FileStorageException("Cloudinary image upload failed", new IOException("boom")));

        mockMvc.perform(multipart("/user/upload-profile-picture").file(pngPart())
                        .header(AUTH_HEADER, bearer(user.token())))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("BAD_GATEWAY"))
                .andExpect(jsonPath("$.message")
                        .value("File storage is temporarily unavailable. Please try again later."));
    }
}
