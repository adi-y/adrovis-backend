package com.adrovis.adrovis_backend.program.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProgramConfigurationRequest(

        @NotNull(message = "feeEnabled is required.")
        Boolean feeEnabled,

        @NotNull(message = "feeAmount is required.")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "feeAmount cannot be negative."
        )
        BigDecimal feeAmount,

        @NotBlank(message = "currency is required.")
        String currency
) {
}