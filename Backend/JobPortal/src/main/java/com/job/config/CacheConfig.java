package com.job.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.time.Duration;
import java.util.List;

/**
 * In-memory caches for the most-read job data (single app instance, so no distributed cache).
 *
 * <p>Ordering: the transaction advisor that Boot registers via {@code @EnableTransactionManagement}
 * uses the default order, {@link Ordered#LOWEST_PRECEDENCE}. Giving the caching advisor a smaller
 * order value makes it the outer interceptor around {@code @Transactional} methods, so
 * {@code @CacheEvict} runs only after the transaction has committed (a concurrent read can't
 * repopulate the cache with the pre-commit row) and a cache hit returns without opening a
 * transaction at all. JobCacheIntegrationTest asserts this advisor order.
 *
 * <p>Deliberately not cached: the four job searches (free-text keys, poor hit rate; title/location
 * cost is better fixed by indexes, see D3) and an employer's own jobs (low read volume, no shared
 * benefit).
 */
@Configuration
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 1)
public class CacheConfig {

    public static final String JOB_BY_ID = "jobById";
    public static final String JOB_PAGES = "jobPages";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // Static cache set: an unknown cache name in an annotation fails instead of silently
        // creating an unbounded default cache.
        cacheManager.setCacheNames(List.of());
        cacheManager.registerCustomCache(JOB_BY_ID, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(1_000)
                .recordStats()
                .build());
        cacheManager.registerCustomCache(JOB_PAGES, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .maximumSize(50)
                .recordStats()
                .build());
        return cacheManager;
    }
}
