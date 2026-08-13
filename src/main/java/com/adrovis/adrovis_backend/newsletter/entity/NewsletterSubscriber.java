package com.adrovis.adrovis_backend.newsletter.entity;

import com.adrovis.adrovis_backend.newsletter.enums.NewsletterSubscriberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "newsletter_subscriber",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_newsletter_subscriber_email",
                        columnNames = "email"
                )
        },
        indexes = {
                @Index(
                        name = "idx_newsletter_subscriber_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_newsletter_subscriber_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "email",
            nullable = false,
            length = 150
    )
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private NewsletterSubscriberStatus status;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = NewsletterSubscriberStatus.SUBSCRIBED;
        }

        if (subscribedAt == null) {
            subscribedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}