package com.job.integration;

import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Every client-facing error -- whether thrown by application code or by Spring's own request
 * parsing -- must come back through {@code GlobalExceptionHandler} with the same JSON shape and
 * never a stack trace or bare 500.
 */
class ErrorMappingIntegrationTest extends AbstractIntegrationTest {

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/jobs/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpectAll(shapeMatchers());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        AuthedUser employer = createEmployer("errmalformed");

        mockMvc.perform(post("/jobs")
                        .header(AUTH_HEADER, bearer(employer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not valid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpectAll(shapeMatchers());
    }

    @Test
    void emptyBodyReturns400() throws Exception {
        AuthedUser employer = createEmployer("errempty");

        mockMvc.perform(post("/jobs")
                        .header(AUTH_HEADER, bearer(employer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpectAll(shapeMatchers());
    }

    @Test
    void textPlainContentTypeReturns415() throws Exception {
        AuthedUser employer = createEmployer("errtextplain");

        mockMvc.perform(post("/jobs")
                        .header(AUTH_HEADER, bearer(employer.token()))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(jobJson("Text plain job")))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpectAll(shapeMatchers());
    }

    @Test
    void wrongMethodReturns405() throws Exception {
        // /jobs/{id} only supports GET, PUT and DELETE. Authenticated so the request actually
        // reaches the DispatcherServlet instead of being stopped at the security filter chain.
        AuthedUser employer = createEmployer("errmethod");

        mockMvc.perform(patch("/jobs/1").header(AUTH_HEADER, bearer(employer.token())))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"))
                .andExpectAll(shapeMatchers());
    }

    @Test
    void negativePageReturns400() throws Exception {
        mockMvc.perform(get("/jobs").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpectAll(shapeMatchers());
    }

    @Test
    void sizeOver100Returns400() throws Exception {
        mockMvc.perform(get("/jobs").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpectAll(shapeMatchers());
    }

    @Test
    void unmappedPathReturns404WithTokenAnd401Without() throws Exception {
        mockMvc.perform(get("/this-route-does-not-exist"))
                .andExpect(status().isUnauthorized())
                .andExpectAll(shapeMatchers());

        AuthedUser seeker = createJobSeeker("errunmapped");
        mockMvc.perform(get("/this-route-does-not-exist")
                        .header(AUTH_HEADER, bearer(seeker.token())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpectAll(shapeMatchers());
    }

    @Test
    void errorBodyNeverLeaksStackTraceOrClassName() throws Exception {
        mockMvc.perform(get("/jobs/abc"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.message").value(not(containsString("com.job"))));
    }

    private ResultMatcher[] shapeMatchers() {
        return new ResultMatcher[]{
                jsonPath("$.status").exists(),
                jsonPath("$.message").exists(),
                jsonPath("$.timestamp").exists(),
                jsonPath("$.error").exists(),
                jsonPath("$.path").exists(),
                jsonPath("$.trace").doesNotExist(),
        };
    }
}
