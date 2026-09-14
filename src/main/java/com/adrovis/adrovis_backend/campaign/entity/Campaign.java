package com.adrovis.adrovis_backend.campaign.entity;

import com.adrovis.adrovis_backend.campaign.enums.CampaignStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignType;
import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Table(
        name = "campaign",
        indexes = {
                @Index(
                        name = "idx_campaign_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_campaign_scheduled_at",
                        columnList = "scheduled_at"
                ),
                @Index(
                        name = "idx_campaign_automation_key",
                        columnList = "automation_key"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campaign extends BaseAuditableEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CampaignType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "automation_key", length = 100, unique = true)
    private String automationKey;

    /*
     * Kept for compatibility/reporting.
     * Candidate-level week progression is now stored on CampaignRecipient.
     */
    @Column(name = "total_weeks", nullable = false)
    private Integer totalWeeks = 4;

    @Column(name = "current_week", nullable = false)
    private Integer currentWeek = 0;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(
            name = "program_name_snapshot",
            nullable = false,
            length = 150
    )
    private String programNameSnapshot;

    @Column(
            name = "fee_amount_snapshot",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal feeAmountSnapshot;

    @Column(
            name = "currency_snapshot",
            nullable = false,
            length = 3
    )
    private String currencySnapshot;

    @Column(
            name = "duration_snapshot",
            nullable = false,
            length = 50
    )
    private String durationSnapshot;

    @Column(
            name = "mode_snapshot",
            nullable = false,
            length = 50
    )
    private String modeSnapshot;

    /**
     * Existing/manual campaign constructor.
     */
    public Campaign(
            String name,
            CampaignType type,
            String programNameSnapshot,
            BigDecimal feeAmountSnapshot,
            String currencySnapshot,
            String durationSnapshot,
            String modeSnapshot
    ) {
        this(
                name,
                type,
                null,
                programNameSnapshot,
                feeAmountSnapshot,
                currencySnapshot,
                durationSnapshot,
                modeSnapshot
        );
    }

    /**
     * Automated campaign constructor.
     */
    public Campaign(
            String name,
            CampaignType type,
            String automationKey,
            String programNameSnapshot,
            BigDecimal feeAmountSnapshot,
            String currencySnapshot,
            String durationSnapshot,
            String modeSnapshot
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Campaign name is required.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Campaign type is required.");
        }

        if (programNameSnapshot == null || programNameSnapshot.isBlank()) {
            throw new IllegalArgumentException(
                    "Program name snapshot is required."
            );
        }

        if (feeAmountSnapshot == null || feeAmountSnapshot.signum() < 0) {
            throw new IllegalArgumentException(
                    "Campaign fee snapshot cannot be negative."
            );
        }

        if (currencySnapshot == null || currencySnapshot.isBlank()) {
            throw new IllegalArgumentException(
                    "Campaign currency snapshot is required."
            );
        }

        if (durationSnapshot == null || durationSnapshot.isBlank()) {
            throw new IllegalArgumentException(
                    "Campaign duration snapshot is required."
            );
        }

        if (modeSnapshot == null || modeSnapshot.isBlank()) {
            throw new IllegalArgumentException(
                    "Campaign mode snapshot is required."
            );
        }

        this.name = name.trim();
        this.type = type;
        this.automationKey =
                automationKey == null || automationKey.isBlank()
                        ? null
                        : automationKey.trim();

        this.programNameSnapshot = programNameSnapshot.trim();
        this.feeAmountSnapshot = feeAmountSnapshot;
        this.currencySnapshot = currencySnapshot.trim().toUpperCase();
        this.durationSnapshot = durationSnapshot.trim();
        this.modeSnapshot = modeSnapshot.trim();
    }

    public void schedule(Instant scheduledAt) {

        if (scheduledAt == null) {
            throw new IllegalArgumentException(
                    "Scheduled time is required."
            );
        }

        this.scheduledAt = scheduledAt;
        this.status = CampaignStatus.SCHEDULED;
    }

    public void start() {

        if (this.status != CampaignStatus.RUNNING) {
            this.status = CampaignStatus.RUNNING;
        }

        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public void complete() {

        this.status = CampaignStatus.COMPLETED;

        if (this.completedAt == null) {
            this.completedAt = Instant.now();
        }
    }

    public void close() {

        this.status = CampaignStatus.CLOSED;

        if (this.closedAt == null) {
            this.closedAt = Instant.now();
        }
    }

    public void cancel() {
        this.status = CampaignStatus.CANCELLED;
    }

    public void advanceWeek(int week) {

        if (week < 0 || week > totalWeeks) {
            throw new IllegalArgumentException(
                    "Campaign week must be between 0 and "
                            + totalWeeks
                            + "."
            );
        }

        this.currentWeek = week;
    }
}