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
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        dto.setName("Updated Name");
        dto.setUsername(user.username());
        dto.setEmail(user.username() + "@example.com");
        dto.setDob(LocalDate.of(1992, 6, 15));
        return dto;
    }

    @Test
    void updatingToAnotherUsersUsernameReturns409() throws Exception {
        AuthedUser userA = createJobSeeker("profdupun_a");
        AuthedUser userB = createJobSeeker("profdupun_b");

        UpdateProfileRequestDTO dto = validUpdate(userB);
        dto.setUsername(userA.username());

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

        UpdateProfileRequestDTO dto = validUpdate(userB);
        dto.setEmail(userA.username() + "@example.com");

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
        UpdateProfileRequestDTO dto = validUpdate(user);
        dto.setName("N".repeat(101)); // @Size(max = 100)

        mockMvc.perform(put("/user/jobseeker/update-profile")
                        .header(AUTH_HEADER, bearer(user.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("name")));
    }
}
