package com.job.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    private static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
            "https://full-stack-job-portal-three.vercel.app",
            "https://full-stack-job-portal-git-main-ahmeds-projects-cc4d86f7.vercel.app",
            "https://jobportal.dev",
            "https://www.jobportal.dev",
            "http://localhost:5173"
    );

    @Value("${cors.allowed-origins:}")
    private String allowedOriginsProperty;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(resolveAllowedOrigins());
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    private List<String> resolveAllowedOrigins() {
        if (allowedOriginsProperty == null || allowedOriginsProperty.isBlank()) {
            return DEFAULT_ALLOWED_ORIGINS;
        }
        return Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }
}
