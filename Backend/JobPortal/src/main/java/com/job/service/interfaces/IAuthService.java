package com.job.service.interfaces;

import com.job.dto.request.LoginRequestDTO;
import com.job.dto.response.AuthResponseDTO;

public interface IAuthService {
    AuthResponseDTO login(LoginRequestDTO dto);
}
