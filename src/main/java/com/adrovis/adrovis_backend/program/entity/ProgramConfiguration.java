package com.adrovis.adrovis_backend.program.entity;

import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "program_configuration",
        indexes = {
                @Index(
                        name = "idx_program_configuration_active",
                        columnList = "active"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProgramConfiguration extends BaseAuditableEntity {

    @Column(
            name = "program_key",
            nullable = false,
            unique = true,
            length = 100
    )
    private String programKey;

    @Column(
            name = "program_name",
            nullable = false,
            length = 150
    )
    private String programName;

    @Column(
            name = "fee_enabled",
            nullable = false
    )
    private boolean feeEnabled;

    @Column(
            name = "fee_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal feeAmount;

    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    private String currency;

    @Column(
            name = "duration",
            nullable = false,
            length = 50
    )
    private String duration;

    @Column(
            name = "mode",
            nullable = false,
            length = 50
    )
    private String mode;

    @Column(
            name = "application_url",
            nullable = false,
            length = 500
    )
    private String applicationUrl;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active = true;

    public ProgramConfiguration(
            String programKey,
            String programName,
            boolean feeEnabled,
            BigDecimal feeAmount,
            String currency,
            String duration,
            String mode,
            String applicationUrl
    ) {
        if (programKey == null || programKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Program key is required."
            );
        }

        if (programName == null || programName.isBlank()) {
            throw new IllegalArgumentException(
                    "Program name is required."
            );
        }

        if (feeAmount == null || feeAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Program fee cannot be negative."
            );
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency is required."
            );
        }

        if (duration == null || duration.isBlank()) {
            throw new IllegalArgumentException(
                    "Duration is required."
            );
        }

        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException(
                    "Mode is required."
            );
        }

        if (applicationUrl == null || applicationUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Application URL is required."
            );
        }

        this.programKey = programKey.trim();
        this.programName = programName.trim();
        this.feeEnabled = feeEnabled;
        this.feeAmount = feeAmount;
        this.currency = currency.trim().toUpperCase();
        this.duration = duration.trim();
        this.mode = mode.trim();
        this.applicationUrl = applicationUrl.trim();
    }

    public void updateCommercialConfiguration(
            boolean feeEnabled,
            BigDecimal feeAmount,
            String currency
    ) {
        if (feeAmount == null || feeAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Program fee cannot be negative."
            );
        }

        this.feeEnabled = feeEnabled;
        this.feeAmount = feeAmount;
        this.currency = currency.trim().toUpperCase();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}