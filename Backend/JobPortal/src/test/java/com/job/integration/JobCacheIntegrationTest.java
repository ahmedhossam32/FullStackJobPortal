package com.job.integration;

import com.github.benmanes.caffeine.cache.Cache;
import com.job.config.CacheConfig;
import com.job.integration.support.AbstractIntegrationTest;
import com.job.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Job caching (D4). Rows are changed directly through the repository, bypassing JobService, so
 * the change is invisible to the cache: an unchanged API response then proves it was served from
 * the cache, and a changed one after a real API write proves that write evicted it.
 */
class JobCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired private JobService jobService;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    void jobDetailIsServedFromCacheUntilUpdateEvictsIt() throws Exception {
        AuthedUser employer = createEmployer("cachedetailemp");
        long jobId = createJob(employer.token(), "Cached Title");
        long hitsBefore = nativeCache(CacheConfig.JOB_BY_ID).stats().hitCount();

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(jsonPath("$.title").value("Cached Title"));

        renameJobBypassingService(jobId, "Changed Behind The Cache");

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Cached Title"));
        assertEquals(hitsBefore + 1, nativeCache(CacheConfig.JOB_BY_ID).stats().hitCount(),
                "the second read should be a recorded cache hit");

        mockMvc.perform(put("/jobs/{id}", jobId)
                        .header(AUTH_HEADER, bearer(employer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson("Updated Through The API")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Through The API"));
    }

    @Test
    void firstListPageIsCachedUntilCreateEvictsIt() throws Exception {
        AuthedUser employer = createEmployer("cachelistemp");
        long existingJobId = createJob(employer.token(), "List Cache Existing");

        // Newest first, so the job just created is on page 0.
        mockMvc.perform(get("/jobs").param("page", "0"))
                .andExpect(jsonPath("$.content[?(@.id == " + existingJobId + ")].title").value("List Cache Existing"));

        renameJobBypassingService(existingJobId, "List Changed Behind The Cache");

        mockMvc.perform(get("/jobs").param("page", "0"))
                .andExpect(jsonPath("$.content[?(@.id == " + existingJobId + ")].title").value("List Cache Existing"));

        long newJobId = createJob(employer.token(), "List Cache New");

        mockMvc.perform(get("/jobs").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + newJobId + ")]").exists())
                .andExpect(jsonPath("$.content[?(@.id == " + existingJobId + ")].title").value("List Changed Behind The Cache"));
    }

    @Test
    void employerProfilePictureUploadEvictsCachedJobs() throws Exception {
        AuthedUser employer = createEmployer("cachepicemp");
        long jobId = createJob(employer.token(), "Logo Job");

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(jsonPath("$.profilePicture").doesNotExist());

        MockMultipartFile png = new MockMultipartFile("file", "logo.png", "image/png", VALID_PNG_BYTES);
        mockMvc.perform(multipart("/user/upload-profile-picture").file(png)
                        .header(AUTH_HEADER, bearer(employer.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePicture").value("https://res.cloudinary.test/image/upload/fake-image.png"));
    }

    @Test
    void largeListPagesAreNotCached() throws Exception {
        AuthedUser employer = createEmployer("cachelargeemp");
        long jobId = createJob(employer.token(), "Large Page Job");

        mockMvc.perform(get("/jobs").param("page", "0").param("size", "50"))
                .andExpect(jsonPath("$.content[?(@.id == " + jobId + ")].title").value("Large Page Job"));

        renameJobBypassingService(jobId, "Large Page Job Renamed");

        mockMvc.perform(get("/jobs").param("page", "0").param("size", "50"))
                .andExpect(jsonPath("$.content[?(@.id == " + jobId + ")].title").value("Large Page Job Renamed"));
    }

    @Test
    void cachingWrapsTheTransactionSoEvictionRunsAfterCommit() {
        // Advisors are listed in invocation order: an earlier advisor is an outer interceptor.
        List<Advisor> advisors = List.of(((Advised) jobService).getAdvisors());
        int cacheIndex = indexOf(advisors, BeanFactoryCacheOperationSourceAdvisor.class);
        int transactionIndex = indexOf(advisors, BeanFactoryTransactionAttributeSourceAdvisor.class);

        assertTrue(cacheIndex >= 0 && transactionIndex >= 0, "expected both advisors on JobService: " + advisors);
        assertTrue(cacheIndex < transactionIndex,
                "caching must wrap @Transactional (evict after commit); advisor order was " + advisors);

        // With equal order values the position above only falls out of bean registration order,
        // so require the declared orders to guarantee it.
        int cacheOrder = ((Ordered) advisors.get(cacheIndex)).getOrder();
        int transactionOrder = ((Ordered) advisors.get(transactionIndex)).getOrder();
        assertTrue(cacheOrder < transactionOrder,
                "caching advisor order " + cacheOrder + " must be lower than transaction advisor order " + transactionOrder);
    }

    private void renameJobBypassingService(long jobId, String title) {
        new TransactionTemplate(transactionManager).executeWithoutResult(tx ->
                jobRepository.findById(jobId).orElseThrow().setTitle(title));
    }

    @SuppressWarnings("unchecked")
    private Cache<Object, Object> nativeCache(String name) {
        return ((CaffeineCache) cacheManager.getCache(name)).getNativeCache();
    }

    private static int indexOf(List<Advisor> advisors, Class<? extends Advisor> type) {
        for (int i = 0; i < advisors.size(); i++) {
            if (type.isInstance(advisors.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
