package com.job.dto.response;

/**
 * Response of {@code GET /user/me}: the shape depends on the caller's role. No shared fields or
 * type discriminator are serialized; this only gives the service a typed return value.
 */
public sealed interface ProfileResponseDTO permits JobSeekerProfileDTO, EmployerProfileDTO {
}
