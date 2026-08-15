package com.adrovis.adrovis_backend.security.service.impl;

import com.adrovis.adrovis_backend.security.dto.request.LoginRequest;
import com.adrovis.adrovis_backend.security.dto.response.LoginResponse;
import com.adrovis.adrovis_backend.security.entity.AdminUser;
import com.adrovis.adrovis_backend.security.jwt.JwtService;
import com.adrovis.adrovis_backend.security.repository.AdminUserRepository;
import com.adrovis.adrovis_backend.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {

        AdminUser adminUser =
                adminUserRepository
                        .findByEmailIgnoreCase(request.getEmail())
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        "Invalid email or password."
                                )
                        );

        if (!adminUser.isEnabled()) {
            throw new BadCredentialsException(
                    "Invalid email or password."
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                adminUser.getPasswordHash()
        )) {
            throw new BadCredentialsException(
                    "Invalid email or password."
            );
        }

        String token =
                jwtService.generateToken(adminUser);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .build();
    }
}