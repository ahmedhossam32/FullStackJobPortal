package com.job.exception;

/**
 * Wraps a failure from the underlying file storage provider (Cloudinary). The provider's own
 * exception message is never surfaced to the client -- GlobalExceptionHandler returns a fixed,
 * generic message and logs this exception's real message and cause internally.
 */
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
