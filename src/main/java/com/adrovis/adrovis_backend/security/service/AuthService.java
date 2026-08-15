package com.adrovis.adrovis_backend.security.service;

import com.adrovis.adrovis_backend.security.dto.request.LoginRequest;
import com.adrovis.adrovis_backend.security.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}