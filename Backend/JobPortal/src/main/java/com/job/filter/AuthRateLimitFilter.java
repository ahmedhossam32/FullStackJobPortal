package com.job.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.job.exception.ErrorResponseWriter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> bucketCache;
    private final int maxRequests;
    private final int windowMinutes;
    private final ErrorResponseWriter errorResponseWriter;

    public AuthRateLimitFilter(
            @Value("${auth.ratelimit.requests:5}") int maxRequests,
            @Value("${auth.ratelimit.window-minutes:1}") int windowMinutes,
            ErrorResponseWriter errorResponseWriter) {
        this.maxRequests = maxRequests;
        this.windowMinutes = windowMinutes;
        this.errorResponseWriter = errorResponseWriter;
        this.bucketCache = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !(path.equals("/auth/signin") || path.startsWith("/auth/signup"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String ip = extractClientIp(request);
        Bucket bucket = bucketCache.get(ip, k -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {} on path: {}", ip, request.getServletPath());
            errorResponseWriter.write(response, HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS",
                    "Too many attempts. Please try again in a minute.", request.getRequestURI());
        }
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(maxRequests,
                Refill.intervally(maxRequests, Duration.ofMinutes(windowMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
