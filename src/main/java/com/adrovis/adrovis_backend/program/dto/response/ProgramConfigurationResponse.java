package com.adrovis.adrovis_backend.program.dto.response;

import com.adrovis.adrovis_backend.program.entity.ProgramConfiguration;

import java.math.BigDecimal;

public record ProgramConfigurationResponse(

        String programKey,
        String programName,
        boolean feeEnabled,
        BigDecimal feeAmount,
        String currency,
        String duration,
        String mode,
        String applicationUrl,
        boolean active
) {

    public static ProgramConfigurationResponse from(
            ProgramConfiguration configuration
    ) {
        return new ProgramConfigurationResponse(
                configuration.getProgramKey(),
                configuration.getProgramName(),
                configuration.isFeeEnabled(),
                configuration.getFeeAmount(),
                configuration.getCurrency(),
                configuration.getDuration(),
                configuration.getMode(),
                configuration.getApplicationUrl(),
                configuration.isActive()
        );
    }
}