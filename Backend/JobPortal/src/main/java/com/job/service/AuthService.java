package com.job.service;

import com.job.dto.request.LoginRequestDTO;
import com.job.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO dto);
}
