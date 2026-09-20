package com.job.integration;

import com.job.enums.JobType;
import com.job.enums.WorkMode;
import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createGetUpdateDeleteAndMyJobsRoundTrip() throws Exception {
        AuthedUser employer = createEmployer("jobscrud");
        long jobId = createJob(employer.token(), "CRUD Job", JobType.FULL_TIME, WorkMode.REMOTE);

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("CRUD Job"))
                .andExpect(jsonPath("$.companyName").exists());

        mockMvc.perform(get("/jobs/my").header(AUTH_HEADER, bearer(employer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + jobId + ")]").exists());

        mockMvc.perform(put("/jobs/{id}", jobId)
                        .header(AUTH_HEADER, bearer(employer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson("CRUD Job Updated", JobType.CONTRACT, WorkMode.ONSITE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("CRUD Job Updated"))
                .andExpect(jsonPath("$.type").value("CONTRACT"))
                .andExpect(jsonPath("$.workMode").value("ONSITE"));

        mockMvc.perform(delete("/jobs/{id}", jobId).header(AUTH_HEADER, bearer(employer.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void searchByTitleFindsCreatedJob() throws Exception {
        AuthedUser employer = createEmployer("jobssearchtitle");
        String uniqueTitle = "Zephyr-" + uniqueUsername("kw");
        createJob(employer.token(), uniqueTitle);

        mockMvc.perform(get("/jobs/search/title").param("keyword", uniqueTitle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(uniqueTitle))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByTypeFindsCreatedJob() throws Exception {
        AuthedUser employer = createEmployer("jobssearchtype");
        String title = "Internship Role " + uniqueUsername("kw");
        createJob(employer.token(), title, JobType.INTERNSHIP, WorkMode.HYBRID);

        mockMvc.perform(get("/jobs/search/type").param("type", "INTERNSHIP").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", hasItem(title)));
    }

    @Test
    void searchByLocationFindsCreatedJob() throws Exception {
        AuthedUser employer = createEmployer("jobssearchloc");
        String title = "Location Role " + uniqueUsername("kw");
        createJob(employer.token(), title);

        mockMvc.perform(get("/jobs/search/location").param("location", "Cairo").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", hasItem(title)));
    }

    @Test
    void searchByWorkModeFindsCreatedJob() throws Exception {
        AuthedUser employer = createEmployer("jobssearchwm");
        String title = "WorkMode Role " + uniqueUsername("kw");
        createJob(employer.token(), title, JobType.PART_TIME, WorkMode.REMOTE);

        mockMvc.perform(get("/jobs/search/workmode").param("workMode", "REMOTE").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", hasItem(title)));
    }

    @Test
    void invalidJobTypeReturns400() throws Exception {
        mockMvc.perform(get("/jobs/search/type").param("type", "NOT_A_TYPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void invalidWorkModeReturns400() throws Exception {
        mockMvc.perform(get("/jobs/search/workmode").param("workMode", "NOT_A_MODE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void missingKeywordReturns400() throws Exception {
        mockMvc.perform(get("/jobs/search/title"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void pageAndSizeBoundsAreEnforced() throws Exception {
        mockMvc.perform(get("/jobs").param("page", "-1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/jobs").param("size", "101")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/jobs").param("size", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/jobs").param("page", "0").param("size", "100")).andExpect(status().isOk());
    }
}
