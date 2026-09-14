package com.adrovis.adrovis_backend.candidate.entity;

import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "candidate",
        indexes = {
                @Index(
                        name = "idx_candidate_normalized_email",
                        columnList = "normalized_email"
                ),
                @Index(
                        name = "idx_candidate_marketing_opt_out",
                        columnList = "marketing_opt_out"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Candidate extends BaseAuditableEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(
            name = "normalized_email",
            nullable = false,
            length = 255,
            unique = true
    )
    private String normalizedEmail;

    @Column(
            name = "marketing_opt_out",
            nullable = false
    )
    private boolean marketingOptOut = false;

    @Column(name = "marketing_opt_out_at")
    private Instant marketingOptOutAt;

    public Candidate(
            String name,
            String email
    ) {
        updateIdentity(name, email);
    }

    public void updateIdentity(
            String name,
            String email
    ) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Candidate email cannot be blank.");
        }

        String normalized = email.trim().toLowerCase();

        this.email = email.trim();
        this.normalizedEmail = normalized;

        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        } else if (this.name == null) {
            this.name = "Candidate";
        }
    }

    public void optOutOfMarketing() {
        if (!this.marketingOptOut) {
            this.marketingOptOut = true;
            this.marketingOptOutAt = Instant.now();
        }
    }

    public void restoreMarketingSubscription() {
        this.marketingOptOut = false;
        this.marketingOptOutAt = null;
    }
}