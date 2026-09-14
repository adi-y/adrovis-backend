package com.adrovis.adrovis_backend.program.repository;

import com.adrovis.adrovis_backend.program.entity.ProgramConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProgramConfigurationRepository
        extends JpaRepository<ProgramConfiguration, UUID> {

    Optional<ProgramConfiguration> findByProgramKey(
            String programKey
    );

    Optional<ProgramConfiguration> findByProgramKeyAndActiveTrue(
            String programKey
    );
}