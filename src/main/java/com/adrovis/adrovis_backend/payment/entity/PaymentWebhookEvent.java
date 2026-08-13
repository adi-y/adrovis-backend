package com.adrovis.adrovis_backend.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payment_webhook_event",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_webhook_event_id",
                        columnNames = "event_id"
                )
        }
)
@Getter
@NoArgsConstructor
public class PaymentWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, length = 150)
    private String eventId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false)
    private Instant receivedAt;

    public PaymentWebhookEvent(
            String eventId,
            String eventType
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.receivedAt = Instant.now();
    }
}