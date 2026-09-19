package com.job.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Writes the same {@link GlobalExceptionHandler.ErrorResponse} JSON shape from places
 * that run outside Spring MVC's exception resolution (security filters, entry points,
 * access-denied handlers), so every error response has one shape regardless of layer.
 */
@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse response, HttpStatus status, String errorCode,
                       String message, String path) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        GlobalExceptionHandler.ErrorResponse body = new GlobalExceptionHandler.ErrorResponse(
                status.value(), message, LocalDateTime.now(), errorCode, path, null, null);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
