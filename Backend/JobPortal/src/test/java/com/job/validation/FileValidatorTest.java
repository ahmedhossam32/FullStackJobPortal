package com.job.validation;

import com.job.exception.InvalidFileException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Plain unit test (no Spring context) for FileValidator's byte-signature checks. Content-Type
 * and filename are deliberately set to misleading values in several cases to prove the
 * validator decides by bytes alone.
 */
class FileValidatorTest {

    private final FileValidator validator = new FileValidator();

    private static final byte[] JPEG_HEADER = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
    private static final byte[] PNG_HEADER = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D
    };
    private static final byte[] GIF87_HEADER = "GIF87a...".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] GIF89_HEADER = "GIF89a...".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP_HEADER = buildWebpHeader();
    private static final byte[] PDF_HEADER = "%PDF-1.4 rest of a minimal pdf".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ZIP_HEADER = {0x50, 0x4B, 0x03, 0x04, 0x14, 0x00}; // DOCX/ZIP local file header

    private static byte[] buildWebpHeader() {
        byte[] header = new byte[16];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, header, 0, 4);
        // bytes 4-7 are the RIFF chunk size, irrelevant to the signature check
        System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, header, 8, 4);
        return header;
    }

    @Test
    void acceptsValidJpeg() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_HEADER);
        assertDoesNotThrow(() -> validator.validateImage(file));
    }

    @Test
    void acceptsValidPng() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", PNG_HEADER);
        assertDoesNotThrow(() -> validator.validateImage(file));
    }

    @Test
    void acceptsValidGif() {
        MockMultipartFile gif87 = new MockMultipartFile("file", "photo.gif", "image/gif", GIF87_HEADER);
        MockMultipartFile gif89 = new MockMultipartFile("file", "photo.gif", "image/gif", GIF89_HEADER);
        assertDoesNotThrow(() -> validator.validateImage(gif87));
        assertDoesNotThrow(() -> validator.validateImage(gif89));
    }

    @Test
    void acceptsValidWebp() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.webp", "image/webp", WEBP_HEADER);
        assertDoesNotThrow(() -> validator.validateImage(file));
    }

    @Test
    void acceptsValidPdf() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", PDF_HEADER);
        assertDoesNotThrow(() -> validator.validateResume(file));
    }

    @Test
    void rejectsSvgWithScriptLabeledAsImage() {
        byte[] svg = "<svg onload=\"x()\"><script>alert(document.cookie)</script></svg>"
                .getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "evil.svg", "image/svg+xml", svg);

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.validateImage(file));
        assertEquals(InvalidFileException.Reason.UNSUPPORTED_TYPE, ex.getReason());
    }

    @Test
    void rejectsTextFileLabeledAsImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "note.txt", "image/png", "just plain text, not an image".getBytes(StandardCharsets.UTF_8));

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.validateImage(file));
        assertEquals(InvalidFileException.Reason.UNSUPPORTED_TYPE, ex.getReason());
    }

    @Test
    void rejectsPdfBytesLabeledAsImage() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "image/png", PDF_HEADER);

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.validateImage(file));
        assertEquals(InvalidFileException.Reason.UNSUPPORTED_TYPE, ex.getReason());
    }

    @Test
    void rejectsZipDocxBytesLabeledAsPdf() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", ZIP_HEADER);

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.validateResume(file));
        assertEquals(InvalidFileException.Reason.UNSUPPORTED_TYPE, ex.getReason());
    }

    @Test
    void rejectsEmptyImageAndResume() {
        MockMultipartFile emptyImage = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        MockMultipartFile emptyResume = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        InvalidFileException imageEx = assertThrows(InvalidFileException.class, () -> validator.validateImage(emptyImage));
        InvalidFileException resumeEx = assertThrows(InvalidFileException.class, () -> validator.validateResume(emptyResume));

        assertEquals(InvalidFileException.Reason.EMPTY, imageEx.getReason());
        assertEquals(InvalidFileException.Reason.EMPTY, resumeEx.getReason());
    }

    @Test
    void rejectsOneToThreeByteFilesAsUnsupportedType() {
        for (int len = 1; len <= 3; len++) {
            byte[] content = new byte[len];
            Arrays.fill(content, (byte) 0xAB);

            MockMultipartFile imageFile = new MockMultipartFile("file", "short.png", "image/png", content);
            MockMultipartFile resumeFile = new MockMultipartFile("file", "short.pdf", "application/pdf", content);

            InvalidFileException imageEx = assertThrows(InvalidFileException.class, () -> validator.validateImage(imageFile));
            InvalidFileException resumeEx = assertThrows(InvalidFileException.class, () -> validator.validateResume(resumeFile));

            assertEquals(InvalidFileException.Reason.UNSUPPORTED_TYPE, imageEx.getReason(), "image length " + len);
            assertEquals(InvalidFileException.Reason.UNSUPPORTED_TYPE, resumeEx.getReason(), "resume length " + len);
        }
    }

    @Test
    void acceptsImageExactlyAtSizeLimit() {
        byte[] content = new byte[(int) FileValidator.MAX_IMAGE_SIZE];
        System.arraycopy(PNG_HEADER, 0, content, 0, PNG_HEADER.length);
        MockMultipartFile file = new MockMultipartFile("file", "max.png", "image/png", content);

        assertDoesNotThrow(() -> validator.validateImage(file));
    }

    @Test
    void rejectsImageOneByteOverSizeLimit() {
        byte[] content = new byte[(int) FileValidator.MAX_IMAGE_SIZE + 1];
        System.arraycopy(PNG_HEADER, 0, content, 0, PNG_HEADER.length);
        MockMultipartFile file = new MockMultipartFile("file", "toobig.png", "image/png", content);

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.validateImage(file));
        assertEquals(InvalidFileException.Reason.TOO_LARGE, ex.getReason());
    }

    @Test
    void acceptsResumeExactlyAtSizeLimit() {
        byte[] content = new byte[(int) FileValidator.MAX_RESUME_SIZE];
        System.arraycopy(PDF_HEADER, 0, content, 0, PDF_HEADER.length);
        MockMultipartFile file = new MockMultipartFile("file", "max.pdf", "application/pdf", content);

        assertDoesNotThrow(() -> validator.validateResume(file));
    }

    @Test
    void rejectsResumeOneByteOverSizeLimit() {
        byte[] content = new byte[(int) FileValidator.MAX_RESUME_SIZE + 1];
        System.arraycopy(PDF_HEADER, 0, content, 0, PDF_HEADER.length);
        MockMultipartFile file = new MockMultipartFile("file", "toobig.pdf", "application/pdf", content);

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.validateResume(file));
        assertEquals(InvalidFileException.Reason.TOO_LARGE, ex.getReason());
    }
}
