package com.adrovis.adrovis_backend.security.config;

import com.adrovis.adrovis_backend.security.entity.AdminUser;
import com.adrovis.adrovis_backend.security.enums.AdminRole;
import com.adrovis.adrovis_backend.security.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.admin.email}")
    private String adminEmail;

    @Value("${security.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        if (adminUserRepository
                .findByEmailIgnoreCase(adminEmail)
                .isPresent()) {

            log.info(
                    "Admin user already exists. Skipping admin provisioning."
            );

            return;
        }

        AdminUser adminUser = AdminUser.builder()
                .email(adminEmail.trim().toLowerCase())
                .passwordHash(
                        passwordEncoder.encode(adminPassword)
                )
                .role(AdminRole.ADMIN)
                .enabled(true)
                .build();

        adminUserRepository.save(adminUser);

        log.info(
                "Initial admin user provisioned successfully."
        );
    }
}