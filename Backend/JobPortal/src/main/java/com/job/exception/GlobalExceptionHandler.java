package com.job.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(
            int status,
            String message,
            LocalDateTime timestamp,
            String error,
            String path,
            List<FieldErrorDetail> fieldErrors,
            String errorId
    ) {
        public record FieldErrorDetail(String field, String message) {}
    }

    // ── Domain exceptions (unchanged status/message, now carry error code + path) ──

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        logClientError(request, HttpStatus.NOT_FOUND, ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        logClientError(request, HttpStatus.FORBIDDEN, ex.getMessage());
        return respond(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        logClientError(request, HttpStatus.CONFLICT, ex.getMessage());
        return respond(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        logClientError(request, HttpStatus.BAD_REQUEST, ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldError> fieldErrorList = ex.getBindingResult().getFieldErrors();
        String message = fieldErrorList.stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        List<ErrorResponse.FieldErrorDetail> fieldErrors = fieldErrorList.stream()
                .map(e -> new ErrorResponse.FieldErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList();
        logClientError(request, HttpStatus.BAD_REQUEST, "validation failed");
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request, fieldErrors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        logClientError(request, HttpStatus.FORBIDDEN, "access denied");
        return respond(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", request);
    }

    // ── Request parsing / binding exceptions Spring throws itself ──

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Malformed or unreadable request body";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            if (field != null) {
                message = "Malformed or unreadable request body: invalid value for field '" + field + "'";
            }
        }
        logClientError(request, HttpStatus.BAD_REQUEST, "unreadable request body");
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String name = ex.getName();
        String value = ex.getValue() == null ? "null" : ex.getValue().toString();
        if (value.length() > 50) {
            value = value.substring(0, 50) + "...";
        }
        StringBuilder message = new StringBuilder("Invalid value for parameter '")
                .append(name).append("': '").append(value).append("'");
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            String allowed = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message.append(". Allowed values: ").append(allowed);
        }
        logClientError(request, HttpStatus.BAD_REQUEST, "type mismatch for parameter '" + name + "'");
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message.toString(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = "Missing required parameter '" + ex.getParameterName() + "'";
        logClientError(request, HttpStatus.BAD_REQUEST, message);
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex, HttpServletRequest request) {
        String message = "Missing required part '" + ex.getRequestPartName() + "'";
        logClientError(request, HttpStatus.BAD_REQUEST, message);
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipart(MultipartException ex, HttpServletRequest request) {
        String message = "Malformed multipart request";
        logClientError(request, HttpStatus.BAD_REQUEST, message);
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint";
        logClientError(request, HttpStatus.METHOD_NOT_ALLOWED, message);
        return respond(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", message, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        String message = "Unsupported Content-Type" + (ex.getContentType() != null ? ": " + ex.getContentType() : "");
        logClientError(request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
        return respond(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", message, request);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
        String message = "None of the acceptable response media types are supported";
        logClientError(request, HttpStatus.NOT_ACCEPTABLE, message);
        return respond(HttpStatus.NOT_ACCEPTABLE, "NOT_ACCEPTABLE", message, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String message = "No endpoint found for " + request.getMethod() + " " + request.getRequestURI();
        logClientError(request, HttpStatus.NOT_FOUND, "no matching route");
        return respond(HttpStatus.NOT_FOUND, "NOT_FOUND", message, request);
    }

    // ── Bean Validation on @RequestParam / @PathVariable ──

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = new ArrayList<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            String name = result.getMethodParameter().getParameterName();
            String paramName = name != null ? name : "parameter";
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                fieldErrors.add(new ErrorResponse.FieldErrorDetail(paramName, error.getDefaultMessage()));
            }
        }
        String message = fieldErrors.stream()
                .map(fe -> fe.field() + ": " + fe.message())
                .collect(Collectors.joining(", "));
        if (message.isBlank()) {
            message = "Invalid request parameters";
        }
        logClientError(request, HttpStatus.BAD_REQUEST, "parameter validation failed");
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> new ErrorResponse.FieldErrorDetail(lastPathNode(v.getPropertyPath()), v.getMessage()))
                .toList();
        String message = fieldErrors.stream()
                .map(fe -> fe.field() + ": " + fe.message())
                .collect(Collectors.joining(", "));
        if (message.isBlank()) {
            message = "Invalid request parameters";
        }
        logClientError(request, HttpStatus.BAD_REQUEST, "constraint violation");
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request, fieldErrors);
    }

    private String lastPathNode(Path path) {
        String last = "parameter";
        for (Path.Node node : path) {
            last = node.getName();
        }
        return last;
    }

    // ── Upload limits ──

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String message = "Uploaded file exceeds the maximum allowed size";
        logClientError(request, HttpStatus.PAYLOAD_TOO_LARGE, message);
        return respond(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE", message, request);
    }

    // ── Database constraint violations, classified by SQLState ──

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String sqlState = extractSqlState(ex);
        if (sqlState != null) {
            switch (sqlState) {
                case "23505" -> {
                    logClientError(request, HttpStatus.CONFLICT, "unique constraint violation");
                    return respond(HttpStatus.CONFLICT, "CONFLICT", "This value already exists", request);
                }
                case "23503" -> {
                    logClientError(request, HttpStatus.CONFLICT, "foreign key constraint violation");
                    return respond(HttpStatus.CONFLICT, "CONFLICT", "This operation references a value that does not exist", request);
                }
                case "22001" -> {
                    logClientError(request, HttpStatus.BAD_REQUEST, "value too long for column");
                    return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "A value is too long", request);
                }
                case "23502" -> {
                    logClientError(request, HttpStatus.BAD_REQUEST, "not-null constraint violation");
                    return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "A required value is missing", request);
                }
                case "23514" -> {
                    logClientError(request, HttpStatus.BAD_REQUEST, "check constraint violation");
                    return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "A value violates a data constraint", request);
                }
                default -> { /* fall through to 500 */ }
            }
        }
        return internalError(ex, request);
    }

    private String extractSqlState(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof SQLException sqlEx) {
                return sqlEx.getSQLState();
            }
            cause = cause.getCause();
        }
        return null;
    }

    // ── Last resort ──

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return internalError(ex, request);
    }

    private ResponseEntity<ErrorResponse> internalError(Exception ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unhandled exception [errorId={}] {} {}", errorId, request.getMethod(), request.getRequestURI(), ex);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred", LocalDateTime.now(),
                "INTERNAL_ERROR", request.getRequestURI(), null, errorId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ── Shared helpers ──

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String errorCode, String message, HttpServletRequest request) {
        return respond(status, errorCode, message, request, null);
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String errorCode, String message,
                                                    HttpServletRequest request,
                                                    List<ErrorResponse.FieldErrorDetail> fieldErrors) {
        ErrorResponse body = new ErrorResponse(status.value(), message, LocalDateTime.now(), errorCode,
                request.getRequestURI(), fieldErrors, null);
        return ResponseEntity.status(status).body(body);
    }

    private void logClientError(HttpServletRequest request, HttpStatus status, String reason) {
        log.warn("{} {} -> {} ({})", request.getMethod(), request.getRequestURI(), status.value(), reason);
    }
}
