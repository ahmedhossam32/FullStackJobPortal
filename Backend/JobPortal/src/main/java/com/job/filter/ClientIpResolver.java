package com.job.filter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the client IP to key rate-limit buckets on. X-Forwarded-For and Forwarded are never
 * trusted (any caller can set them). A single configured header is read instead, and only when
 * its value passes a strict literal-address check -- no DNS resolution is ever performed on
 * untrusted input, so a bad value simply falls back to the connection's remote address rather
 * than being "resolved" some other way.
 */
public class ClientIpResolver {

    private static final int MAX_LENGTH = 45; // longest textual IPv6 (with embedded IPv4) literal
    private static final String LITERAL_ADDRESS_PATTERN = "[0-9A-Fa-f.:]+";

    private final String headerName;

    public ClientIpResolver(String headerName) {
        this.headerName = normalize(headerName);
    }

    private static String normalize(String headerName) {
        if (headerName == null || headerName.isBlank()) {
            return null;
        }
        String trimmed = headerName.trim();
        // These are exactly the headers this resolver exists to stop trusting; never honor them
        // even if misconfigured as the lookup header.
        if (trimmed.equalsIgnoreCase("X-Forwarded-For") || trimmed.equalsIgnoreCase("Forwarded")) {
            return null;
        }
        return trimmed;
    }

    public String resolve(HttpServletRequest request) {
        if (headerName != null) {
            String value = request.getHeader(headerName);
            if (value != null) {
                String trimmed = value.trim();
                if (isLiteralAddress(trimmed)) {
                    return trimmed;
                }
            }
        }
        return request.getRemoteAddr();
    }

    private static boolean isLiteralAddress(String value) {
        return !value.isEmpty() && value.length() <= MAX_LENGTH && value.matches(LITERAL_ADDRESS_PATTERN);
    }
}
