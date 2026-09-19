package com.job.validation;

import com.job.exception.InvalidFileException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Decides what a file actually is by its bytes, never by the client-supplied Content-Type or
 * filename. Only the header bytes needed for a signature match are read.
 */
@Component
public class FileValidator {

    public static final long MAX_IMAGE_SIZE = 2L * 1024 * 1024;
    public static final long MAX_RESUME_SIZE = 5L * 1024 * 1024;

    private static final int IMAGE_HEADER_LENGTH = 12; // enough for WebP's "RIFF"[4 bytes]"WEBP" at offset 8

    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final byte[] GIF87_SIGNATURE = "GIF87a".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] GIF89_SIGNATURE = "GIF89a".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] RIFF_SIGNATURE = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP_SIGNATURE = "WEBP".getBytes(StandardCharsets.US_ASCII);
    private static final int WEBP_SIGNATURE_OFFSET = 8;

    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    public void validateImage(MultipartFile file) {
        requireNonEmpty(file);
        requireWithinSize(file, MAX_IMAGE_SIZE, "Image");
        byte[] header = readHeader(file, IMAGE_HEADER_LENGTH);
        if (!isSupportedImage(header)) {
            throw new InvalidFileException(InvalidFileException.Reason.UNSUPPORTED_TYPE,
                    "Only JPEG, PNG, GIF and WebP images are allowed");
        }
    }

    public void validateResume(MultipartFile file) {
        requireNonEmpty(file);
        requireWithinSize(file, MAX_RESUME_SIZE, "Resume");
        byte[] header = readHeader(file, PDF_SIGNATURE.length);
        if (!matchesAt(header, 0, PDF_SIGNATURE)) {
            throw new InvalidFileException(InvalidFileException.Reason.UNSUPPORTED_TYPE,
                    "Only PDF files are allowed");
        }
    }

    private void requireNonEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException(InvalidFileException.Reason.EMPTY, "File cannot be empty");
        }
    }

    private void requireWithinSize(MultipartFile file, long maxBytes, String label) {
        if (file.getSize() > maxBytes) {
            throw new InvalidFileException(InvalidFileException.Reason.TOO_LARGE,
                    label + " must not exceed " + (maxBytes / (1024 * 1024)) + "MB");
        }
    }

    private boolean isSupportedImage(byte[] header) {
        return matchesAt(header, 0, JPEG_SIGNATURE)
                || matchesAt(header, 0, PNG_SIGNATURE)
                || matchesAt(header, 0, GIF87_SIGNATURE)
                || matchesAt(header, 0, GIF89_SIGNATURE)
                || isWebp(header);
    }

    private boolean isWebp(byte[] header) {
        return matchesAt(header, 0, RIFF_SIGNATURE) && matchesAt(header, WEBP_SIGNATURE_OFFSET, WEBP_SIGNATURE);
    }

    private boolean matchesAt(byte[] data, int offset, byte[] pattern) {
        if (data.length < offset + pattern.length) {
            return false;
        }
        for (int i = 0; i < pattern.length; i++) {
            if (data[offset + i] != pattern[i]) {
                return false;
            }
        }
        return true;
    }

    /** Reads up to {@code length} header bytes. A short or unreadable file yields a shorter (or empty) array. */
    private byte[] readHeader(MultipartFile file, int length) {
        byte[] buffer = new byte[length];
        try (InputStream is = file.getInputStream()) {
            int read = is.readNBytes(buffer, 0, length);
            return read == length ? buffer : Arrays.copyOf(buffer, read);
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
