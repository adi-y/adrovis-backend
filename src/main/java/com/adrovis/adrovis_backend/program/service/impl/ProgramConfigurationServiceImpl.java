package com.adrovis.adrovis_backend.program.service.impl;

import com.adrovis.adrovis_backend.common.exception.AppException;
import com.adrovis.adrovis_backend.program.entity.ProgramConfiguration;
import com.adrovis.adrovis_backend.program.repository.ProgramConfigurationRepository;
import com.adrovis.adrovis_backend.program.service.ProgramConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProgramConfigurationServiceImpl
        implements ProgramConfigurationService {

    private final ProgramConfigurationRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ProgramConfiguration getActive(String programKey) {
        return repository
                .findByProgramKeyAndActiveTrue(programKey)
                .orElseThrow(() ->
                        AppException.notFound(
                                "Active program configuration not found."
                        )
                );
    }

    @Override
    @Transactional
    public ProgramConfiguration update(
            String programKey,
            boolean feeEnabled,
            BigDecimal feeAmount,
            String currency
    ) {
        ProgramConfiguration configuration =
                repository
                        .findByProgramKey(programKey)
                        .orElseThrow(() ->
                                AppException.notFound(
                                        "Program configuration not found."
                                )
                        );

        configuration.updateCommercialConfiguration(
                feeEnabled,
                feeAmount,
                currency
        );

        return configuration;
    }
}