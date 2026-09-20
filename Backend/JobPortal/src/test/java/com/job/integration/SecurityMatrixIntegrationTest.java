package com.job.integration;

import com.job.dto.request.ApplicationRequestDTO;
import com.job.dto.request.ApplicationStatusUpdateDTO;
import com.job.dto.request.EmployerRegisterRequestDTO;
import com.job.dto.request.JobSeekerRegisterRequestDTO;
import com.job.dto.request.LoginRequestDTO;
import com.job.dto.request.UpdateProfileRequestDTO;
import com.job.enums.ApplicationStatus;
import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Data-driven access-control matrix over every one of the app's 34 endpoints (baseline
 * inventory: 3 auth, 10 job, 9 application, 7 user, 5 notification). For each endpoint:
 * no token and a garbage token behave identically (both are "anonymous" as far as
 * {@code JwtAuthFilter} is concerned) -- 401 JSON for a protected endpoint, unblocked for a
 * public one; a validly-signed token for the wrong role is 403 JSON; the allowed role(s) are
 * never blocked by security (a 404 from a made-up id is fine -- only 401/403 fail the check).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityMatrixIntegrationTest extends AbstractIntegrationTest {

    private static final long DUMMY_ID = 987_654_321L;

    private AuthedUser seeker;
    private AuthedUser employer;
    private List<EndpointSpec> specs;

    private enum Access { PUBLIC, JOB_SEEKER_ONLY, EMPLOYER_ONLY, ANY_AUTHENTICATED }

    private record EndpointSpec(String name, Access access, Supplier<MockHttpServletRequestBuilder> builder) {
        @Override
        public String toString() {
            return name;
        }
    }

    @BeforeAll
    void setUp() throws Exception {
        seeker = createJobSeeker("secmxsk");
        employer = createEmployer("secmxem");
        specs = buildSpecs();
    }

    Stream<EndpointSpec> specs() {
        return specs.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("specs")
    void endpointEnforcesExpectedAccess(EndpointSpec spec) throws Exception {
        checkNoTokenAndGarbageToken(spec);
        checkWrongRole(spec);
        checkAllowedRole(spec);
    }

    // ── Assertions ───────────────────────────────────────────────────────────

    private void checkNoTokenAndGarbageToken(EndpointSpec spec) throws Exception {
        ResultActions noToken = mockMvc.perform(spec.builder().get());
        ResultActions garbage = mockMvc.perform(spec.builder().get().header(AUTH_HEADER, "Bearer not-a-real-token"));

        if (spec.access() == Access.PUBLIC) {
            assertNotAuthOrForbidden(noToken, spec.name() + " (no token, endpoint should be public)");
            assertNotAuthOrForbidden(garbage, spec.name() + " (garbage token, endpoint should still be public)");
        } else {
            noToken.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
            garbage.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
        }
    }

    private void checkWrongRole(EndpointSpec spec) throws Exception {
        String wrongToken = switch (spec.access()) {
            case EMPLOYER_ONLY -> seeker.token();
            case JOB_SEEKER_ONLY -> employer.token();
            case PUBLIC, ANY_AUTHENTICATED -> null;
        };
        if (wrongToken == null) {
            return;
        }
        mockMvc.perform(spec.builder().get().header(AUTH_HEADER, bearer(wrongToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    private void checkAllowedRole(EndpointSpec spec) throws Exception {
        List<String> allowedTokens = switch (spec.access()) {
            case PUBLIC -> List.of(); // already exercised by the no-token case above
            case EMPLOYER_ONLY -> List.of(employer.token());
            case JOB_SEEKER_ONLY -> List.of(seeker.token());
            case ANY_AUTHENTICATED -> List.of(seeker.token(), employer.token());
        };
        for (String token : allowedTokens) {
            ResultActions result = mockMvc.perform(spec.builder().get().header(AUTH_HEADER, bearer(token)));
            assertNotAuthOrForbidden(result, spec.name() + " (allowed role must not be blocked)");
        }
    }

    private void assertNotAuthOrForbidden(ResultActions actions, String context) throws Exception {
        int status = actions.andReturn().getResponse().getStatus();
        assertTrue(status != 401 && status != 403, context + " -> unexpected status " + status);
    }

    // ── Endpoint inventory (34) ──────────────────────────────────────────────

    private List<EndpointSpec> buildSpecs() {
        List<EndpointSpec> specs = new ArrayList<>();

        // Auth (3) -- all public.
        specs.add(new EndpointSpec("POST /auth/signup/jobseeker", Access.PUBLIC,
                () -> post("/auth/signup/jobseeker").contentType(MediaType.APPLICATION_JSON)
                        .content(newSeekerSignupJson())));
        specs.add(new EndpointSpec("POST /auth/signup/employer", Access.PUBLIC,
                () -> post("/auth/signup/employer").contentType(MediaType.APPLICATION_JSON)
                        .content(newEmployerSignupJson())));
        specs.add(new EndpointSpec("POST /auth/signin", Access.PUBLIC,
                () -> post("/auth/signin").contentType(MediaType.APPLICATION_JSON)
                        .content(validSignInJson())));

        // Jobs (10).
        specs.add(new EndpointSpec("POST /jobs", Access.EMPLOYER_ONLY,
                () -> post("/jobs").contentType(MediaType.APPLICATION_JSON).content(quietJobJson("Matrix Job"))));
        specs.add(new EndpointSpec("GET /jobs/my", Access.EMPLOYER_ONLY, () -> get("/jobs/my")));
        specs.add(new EndpointSpec("PUT /jobs/{id}", Access.EMPLOYER_ONLY,
                () -> put("/jobs/{id}", DUMMY_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(quietJobJson("Matrix Job Updated"))));
        specs.add(new EndpointSpec("DELETE /jobs/{id}", Access.EMPLOYER_ONLY,
                () -> delete("/jobs/{id}", DUMMY_ID)));
        specs.add(new EndpointSpec("GET /jobs", Access.PUBLIC, () -> get("/jobs")));
        specs.add(new EndpointSpec("GET /jobs/{id}", Access.PUBLIC, () -> get("/jobs/{id}", DUMMY_ID)));
        specs.add(new EndpointSpec("GET /jobs/search/title", Access.PUBLIC,
                () -> get("/jobs/search/title").param("keyword", "engineer")));
        specs.add(new EndpointSpec("GET /jobs/search/type", Access.PUBLIC,
                () -> get("/jobs/search/type").param("type", "FULL_TIME")));
        specs.add(new EndpointSpec("GET /jobs/search/location", Access.PUBLIC,
                () -> get("/jobs/search/location").param("location", "Cairo")));
        specs.add(new EndpointSpec("GET /jobs/search/workmode", Access.PUBLIC,
                () -> get("/jobs/search/workmode").param("workMode", "REMOTE")));

        // Applications (9).
        specs.add(new EndpointSpec("POST /applications", Access.JOB_SEEKER_ONLY,
                () -> post("/applications").contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(DUMMY_ID))));
        specs.add(new EndpointSpec("GET /applications/has-applied/{jobId}", Access.JOB_SEEKER_ONLY,
                () -> get("/applications/has-applied/{jobId}", DUMMY_ID)));
        specs.add(new EndpointSpec("GET /applications/my", Access.JOB_SEEKER_ONLY, () -> get("/applications/my")));
        specs.add(new EndpointSpec("GET /applications/{id}", Access.JOB_SEEKER_ONLY,
                () -> get("/applications/{id}", DUMMY_ID)));
        specs.add(new EndpointSpec("DELETE /applications/{id}", Access.JOB_SEEKER_ONLY,
                () -> delete("/applications/{id}", DUMMY_ID)));
        specs.add(new EndpointSpec("GET /applications/job/{jobId}", Access.EMPLOYER_ONLY,
                () -> get("/applications/job/{jobId}", DUMMY_ID)));
        specs.add(new EndpointSpec("GET /applications/employer/{id}", Access.EMPLOYER_ONLY,
                () -> get("/applications/employer/{id}", DUMMY_ID)));
        specs.add(new EndpointSpec("PUT /applications/{id}/status", Access.EMPLOYER_ONLY,
                () -> put("/applications/{id}/status", DUMMY_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("REVIEWED"))));
        specs.add(new EndpointSpec("GET /applications/employer", Access.EMPLOYER_ONLY,
                () -> get("/applications/employer")));

        // User (7).
        specs.add(new EndpointSpec("POST /user/jobseeker/upload-resume", Access.JOB_SEEKER_ONLY,
                () -> multipart("/user/jobseeker/upload-resume").file(pdfPart())));
        specs.add(new EndpointSpec("POST /user/upload-profile-picture", Access.ANY_AUTHENTICATED,
                () -> multipart("/user/upload-profile-picture").file(pngPart())));
        specs.add(new EndpointSpec("POST /user/save-job/{jobId}", Access.JOB_SEEKER_ONLY,
                () -> post("/user/save-job/{jobId}", DUMMY_ID)));
        specs.add(new EndpointSpec("PUT /user/jobseeker/update-profile", Access.JOB_SEEKER_ONLY,
                () -> put("/user/jobseeker/update-profile").contentType(MediaType.APPLICATION_JSON)
                        .content(updateProfileJson())));
        specs.add(new EndpointSpec("DELETE /user/unsave-job/{jobId}", Access.JOB_SEEKER_ONLY,
                () -> delete("/user/unsave-job/{jobId}", DUMMY_ID)));
        specs.add(new EndpointSpec("GET /user/saved-jobs", Access.JOB_SEEKER_ONLY, () -> get("/user/saved-jobs")));
        specs.add(new EndpointSpec("GET /user/me", Access.ANY_AUTHENTICATED, () -> get("/user/me")));

        // Notifications (5).
        specs.add(new EndpointSpec("GET /notifications", Access.JOB_SEEKER_ONLY, () -> get("/notifications")));
        specs.add(new EndpointSpec("DELETE /notifications", Access.JOB_SEEKER_ONLY, () -> delete("/notifications")));
        specs.add(new EndpointSpec("PUT /notifications/{id}/read", Access.JOB_SEEKER_ONLY,
                () -> put("/notifications/{id}/read", DUMMY_ID)));
        specs.add(new EndpointSpec("GET /notifications/unread-count", Access.JOB_SEEKER_ONLY,
                () -> get("/notifications/unread-count")));
        specs.add(new EndpointSpec("GET /notifications/unread", Access.JOB_SEEKER_ONLY,
                () -> get("/notifications/unread")));

        return specs;
    }

    // ── Body builders (each generates fresh unique data per call) ───────────

    private String newSeekerSignupJson() {
        String username = uniqueUsername("mxsk");
        JobSeekerRegisterRequestDTO dto = new JobSeekerRegisterRequestDTO();
        dto.setName("Matrix Seeker");
        dto.setUsername(username);
        dto.setPassword(DEFAULT_PASSWORD);
        dto.setDob(LocalDate.of(1998, 5, 5));
        dto.setEmail(username + "@example.com");
        return writeJson(dto);
    }

    private String newEmployerSignupJson() {
        String username = uniqueUsername("mxem");
        EmployerRegisterRequestDTO dto = new EmployerRegisterRequestDTO();
        dto.setName("Matrix Employer");
        dto.setUsername(username);
        dto.setPassword(DEFAULT_PASSWORD);
        dto.setCompanyName("Matrix Co");
        dto.setEmail(username + "@example.com");
        dto.setIndustry("Tech");
        return writeJson(dto);
    }

    private String validSignInJson() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername(seeker.username());
        dto.setPassword(seeker.password());
        return writeJson(dto);
    }

    private String quietJobJson(String title) {
        try {
            return jobJson(title);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String applicationJson(long jobId) {
        ApplicationRequestDTO dto = new ApplicationRequestDTO();
        dto.setJobId(jobId);
        return writeJson(dto);
    }

    private String statusJson(String status) {
        ApplicationStatusUpdateDTO dto = new ApplicationStatusUpdateDTO();
        dto.setStatus(ApplicationStatus.valueOf(status));
        return writeJson(dto);
    }

    /**
     * Deliberately keeps the seeker's own username/email unchanged (a no-op update). Renaming it
     * would invalidate the shared {@code seeker} JWT for every later spec in this same run --
     * {@code JwtAuthFilter} looks the user up by the username baked into the token's subject.
     */
    private String updateProfileJson() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        dto.setName("Matrix Updated");
        dto.setUsername(seeker.username());
        dto.setEmail(seeker.username() + "@example.com");
        dto.setDob(LocalDate.of(1990, 1, 1));
        return writeJson(dto);
    }

    private String writeJson(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
