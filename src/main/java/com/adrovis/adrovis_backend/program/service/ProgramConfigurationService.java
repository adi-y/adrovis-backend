package com.adrovis.adrovis_backend.program.service;

import com.adrovis.adrovis_backend.program.entity.ProgramConfiguration;

import java.math.BigDecimal;

public interface ProgramConfigurationService {

    ProgramConfiguration getActive(String programKey);

    ProgramConfiguration update(
            String programKey,
            boolean feeEnabled,
            BigDecimal feeAmount,
            String currency
    );
}