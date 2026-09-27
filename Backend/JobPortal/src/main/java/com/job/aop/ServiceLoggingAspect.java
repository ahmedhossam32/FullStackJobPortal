package com.job.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.RecordComponent;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * Entry (DEBUG) and exit (INFO, with duration) logging for the business services. Both lines carry
 * the redacted argument summary, so INFO alone shows which call ran with what.
 * Exceptions pass through untouched and are not logged here: every one of these services is only
 * called from controllers, so GlobalExceptionHandler logs each failure exactly once.
 * EmailServiceImpl and CloudinaryService are deliberately not covered; they keep their own logging.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ServiceLoggingAspect {

    private static final String REDACTED = "[REDACTED]";
    // Credentials: redacted wherever the name contains one of these.
    private static final String[] CREDENTIAL_NAME_PARTS = {"password", "token", "secret"};
    // Personal data (E1): "email"/"dob" also cover names like applicantEmail. "name" must match
    // exactly, so identifiers such as username and companyName stay visible.
    private static final String[] PERSONAL_DATA_NAME_PARTS = {"email", "dob", "dateofbirth"};
    private static final String[] PERSONAL_DATA_EXACT_NAMES = {"name"};

    @Pointcut("within(com.job.service.impl.JobServiceImpl)"
            + " || within(com.job.service.impl.ApplicationServiceImpl)"
            + " || within(com.job.service.impl.NotificationServiceImpl)"
            + " || within(com.job.service.impl.ProfileServiceImpl)"
            + " || within(com.job.service.impl.SavedJobServiceImpl)"
            + " || within(com.job.service.impl.AuthServiceImpl)"
            + " || within(com.job.service.impl.UserServiceImpl)")
    void businessServices() {
    }

    @Around("businessServices()")
    public Object logInvocation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Logger log = LoggerFactory.getLogger(signature.getDeclaringType());
        String method = signature.getName();

        String arguments = log.isInfoEnabled()
                ? describeArguments(signature.getParameterNames(), joinPoint.getArgs())
                : null;
        log.debug("{} called with {}", method, arguments);

        long start = System.nanoTime();
        Object result = joinPoint.proceed();
        log.info("{} {} completed in {} ms", method, arguments, (System.nanoTime() - start) / 1_000_000);
        return result;
    }

    private static String describeArguments(String[] names, Object[] args) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < args.length; i++) {
            String name = names != null ? names[i] : "arg" + i;
            joiner.add(name + "=" + (isSensitive(name) ? REDACTED : describe(args[i])));
        }
        return joiner.toString();
    }

    // Records are expanded with sensitive components redacted. Any other non-simple object is
    // logged by type only, so an unknown toString() can never leak a secret.
    private static String describe(Object value) {
        if (value == null || value instanceof CharSequence || value instanceof Number
                || value instanceof Boolean || value instanceof Enum<?> || value instanceof Temporal) {
            return String.valueOf(value);
        }
        if (value instanceof MultipartFile file) {
            return "MultipartFile[name=" + file.getOriginalFilename() + ", size=" + file.getSize() + "]";
        }
        if (value.getClass().isRecord()) {
            return describeRecord(value);
        }
        if (value instanceof Iterable<?> items) {
            StringJoiner joiner = new StringJoiner(", ", "[", "]");
            items.forEach(item -> joiner.add(describe(item)));
            return joiner.toString();
        }
        return value.getClass().getSimpleName();
    }

    private static String describeRecord(Object record) {
        StringJoiner joiner = new StringJoiner(", ", record.getClass().getSimpleName() + "[", "]");
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            String name = component.getName();
            String rendered;
            if (isSensitive(name)) {
                rendered = REDACTED;
            } else {
                try {
                    rendered = describe(component.getAccessor().invoke(record));
                } catch (ReflectiveOperationException e) {
                    rendered = "?";
                }
            }
            joiner.add(name + "=" + rendered);
        }
        return joiner.toString();
    }

    private static boolean isSensitive(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        for (String part : CREDENTIAL_NAME_PARTS) {
            if (lower.contains(part)) {
                return true;
            }
        }
        for (String part : PERSONAL_DATA_NAME_PARTS) {
            if (lower.contains(part)) {
                return true;
            }
        }
        for (String exact : PERSONAL_DATA_EXACT_NAMES) {
            if (lower.equals(exact)) {
                return true;
            }
        }
        return false;
    }
}
