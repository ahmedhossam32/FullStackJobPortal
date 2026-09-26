package com.job.integration;

import com.job.dto.request.UpdateProfileRequestDTO;
import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileIntegrationTest extends AbstractIntegrationTest {

    private UpdateProfileRequestDTO validUpdate(AuthedUser user) {
        return new UpdateProfileRequestDTO(
                "Updated Name", user.username(), user.username() + "@example.com", LocalDate.of(1992, 6, 15));
    }

    @Test
    void updatingToAnotherUsersUsernameReturns409() throws Exception {
        AuthedUser userA = createJobSeeker("profdupun_a");
        AuthedUser userB = createJobSeeker("profdupun_b");

        UpdateProfileRequestDTO base = validUpdate(userB);
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO(base.name(), userA.username(), base.email(), base.dob());

        mockMvc.perform(put("/user/jobseeker/update-profile")
                        .header(AUTH_HEADER, bearer(userB.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void updatingToAnotherUsersEmailReturns409() throws Exception {
        AuthedUser userA = createJobSeeker("profdupem_a");
        AuthedUser userB = createJobSeeker("profdupem_b");

        UpdateProfileRequestDTO base = validUpdate(userB);
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO(
                base.name(), base.username(), userA.username() + "@example.com", base.dob());

        mockMvc.perform(put("/user/jobseeker/update-profile")
                        .header(AUTH_HEADER, bearer(userB.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void updatingWithOwnUnchangedUsernameReturns200() throws Exception {
        AuthedUser user = createJobSeeker("profsameun");

        mockMvc.perform(put("/user/jobseeker/update-profile")
                        .header(AUTH_HEADER, bearer(user.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdate(user))))
                .andExpect(status().isOk());
    }

    @Test
    void overLengthFieldsReturn400WithFieldErrors() throws Exception {
        AuthedUser user = createJobSeeker("profoverlen");
        UpdateProfileRequestDTO base = validUpdate(user);
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO(
                "N".repeat(101), base.username(), base.email(), base.dob()); // @Size(max = 100)

        mockMvc.perform(put("/user/jobseeker/update-profile")
                        .header(AUTH_HEADER, bearer(user.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("name")));
    }
}
