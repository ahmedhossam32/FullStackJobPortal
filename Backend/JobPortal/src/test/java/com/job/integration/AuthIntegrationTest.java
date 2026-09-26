package com.job.integration;

import com.job.dto.request.EmployerRegisterRequestDTO;
import com.job.dto.request.JobSeekerRegisterRequestDTO;
import com.job.dto.request.LoginRequestDTO;
import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end signup / sign-in behavior, hitting the real controller, service and database. */
class AuthIntegrationTest extends AbstractIntegrationTest {

    private JobSeekerRegisterRequestDTO validSeeker(String username) {
        return new JobSeekerRegisterRequestDTO(
                "Alice Seeker", username, DEFAULT_PASSWORD, LocalDate.of(1997, 3, 4), username + "@example.com");
    }

    @Test
    void signupHappyPathReturns201WithContractWording() throws Exception {
        String username = uniqueUsername("authhappy");

        mockMvc.perform(post("/auth/signup/jobseeker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSeeker(username))))
                .andExpect(status().isCreated())
                .andExpect(content().string("Job seeker signed up successfully!"));
    }

    @Test
    void duplicateUsernameReturns409() throws Exception {
        String username = uniqueUsername("authdup");
        JobSeekerRegisterRequestDTO first = validSeeker(username);
        mockMvc.perform(post("/auth/signup/jobseeker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        JobSeekerRegisterRequestDTO second = new JobSeekerRegisterRequestDTO(
                first.name(), first.username(), first.password(), first.dob(),
                uniqueUsername("authdup") + "@example.com");
        mockMvc.perform(post("/auth/signup/jobseeker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void missingFieldsReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/auth/signup/jobseeker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("username")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("password")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("email")));
    }

    @Test
    void passwordOver72BytesReturns400() throws Exception {
        JobSeekerRegisterRequestDTO base = validSeeker(uniqueUsername("authpwlen"));
        // 73 ASCII bytes == 73 UTF-8 bytes, one over the BCrypt-driven 72-byte limit.
        JobSeekerRegisterRequestDTO dto = new JobSeekerRegisterRequestDTO(
                base.name(), base.username(), "a".repeat(73), base.dob(), base.email());

        mockMvc.perform(post("/auth/signup/jobseeker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("password")));
    }

    @Test
    void signInOkReturns200WithTokenAndRole() throws Exception {
        String username = uniqueUsername("authok");
        mockMvc.perform(post("/auth/signup/jobseeker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSeeker(username))))
                .andExpect(status().isCreated());

        LoginRequestDTO login = new LoginRequestDTO(username, DEFAULT_PASSWORD);

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("JOB_SEEKER"));
    }

    @Test
    void signInWrongPasswordReturns401() throws Exception {
        String username = uniqueUsername("authwrongpw");
        mockMvc.perform(post("/auth/signup/jobseeker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSeeker(username))))
                .andExpect(status().isCreated());

        LoginRequestDTO login = new LoginRequestDTO(username, "totally-wrong-password");

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signInUnknownUserReturns401() throws Exception {
        LoginRequestDTO login = new LoginRequestDTO(uniqueUsername("nosuchuser"), DEFAULT_PASSWORD);

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signInWith100CharacterPasswordReturns401NotServerError() throws Exception {
        String username = uniqueUsername("authlongpw");
        mockMvc.perform(post("/auth/signup/jobseeker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSeeker(username))))
                .andExpect(status().isCreated());

        LoginRequestDTO login = new LoginRequestDTO(username, "x".repeat(100));

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employerSignupHappyPathReturns201() throws Exception {
        String username = uniqueUsername("authemp");
        EmployerRegisterRequestDTO dto = new EmployerRegisterRequestDTO(
                "Bob Employer", username, DEFAULT_PASSWORD, "Bob's Company", username + "@example.com", "Retail");

        mockMvc.perform(post("/auth/signup/employer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Employer signed up successfully!"));
    }
}
