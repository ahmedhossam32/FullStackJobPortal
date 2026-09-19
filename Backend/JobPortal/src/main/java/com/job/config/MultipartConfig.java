package com.job.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines upload size limits in code rather than in the gitignored properties file or an env
 * var, so they're versioned. Spring Boot's own MultipartAutoConfiguration#multipartConfigElement
 * is annotated @ConditionalOnMissingBean(MultipartConfigElement.class), so this bean replaces it,
 * and DispatcherServletRegistrationConfiguration picks it up via
 * ObjectProvider<MultipartConfigElement> when registering the DispatcherServlet -- the servlet
 * container enforces these limits before any controller runs.
 */
@Configuration
public class MultipartConfig {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_REQUEST_SIZE = 6L * 1024 * 1024;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("", MAX_FILE_SIZE, MAX_REQUEST_SIZE, 0);
    }
}
