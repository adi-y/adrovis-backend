package com.adrovis.adrovis_backend.payment.entity;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payment_transaction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_application",
                        columnNames = "application_id"
                ),
                @UniqueConstraint(
                        name = "uk_payment_reference",
                        columnNames = "reference_id"
                ),
                @UniqueConstraint(
                        name = "uk_payment_link",
                        columnNames = "razorpay_payment_link_id"
                )
        }
)
@Getter
@NoArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false
    )
    private Application application;

    @Column(name = "reference_id", nullable = false, length = 40)
    private String referenceId;

    @Column(
            name = "razorpay_payment_link_id",
            nullable = false,
            length = 100
    )
    private String razorpayPaymentLinkId;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "payment_link_url", nullable = false, columnDefinition = "text")
    private String paymentLinkUrl;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant paidAt;

    private Instant expiredAt;

    private Instant cancelledAt;

    @Column(nullable = false)
    private int failedAttempts = 0;

    private Instant lastFailedAt;

    @Column(columnDefinition = "text")
    private String lastFailureReason;

    @Version
    private long version;

    public PaymentTransaction(
            Application application,
            String referenceId,
            String razorpayPaymentLinkId,
            String razorpayOrderId,
            long amount,
            String currency,
            String paymentLinkUrl
    ) {

        this.application = application;
        this.referenceId = referenceId;
        this.razorpayPaymentLinkId = razorpayPaymentLinkId;
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.paymentLinkUrl = paymentLinkUrl;
        this.status = PaymentStatus.ISSUED;
        this.createdAt = Instant.now();
    }

    public void markPaid(
            String paymentId,
            Instant paidAt
    ) {

        if (this.status == PaymentStatus.PAID) {
            return;
        }

        this.razorpayPaymentId = paymentId;
        this.status = PaymentStatus.PAID;
        this.paidAt = paidAt;
    }

    public void markExpired() {

        if (this.status == PaymentStatus.PAID) {
            return;
        }

        this.status = PaymentStatus.EXPIRED;
        this.expiredAt = Instant.now();
    }

    public void markCancelled() {

        if (this.status == PaymentStatus.PAID) {
            return;
        }

        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public void recordFailedAttempt(String reason) {

        if (this.status == PaymentStatus.PAID) {
            return;
        }

        this.failedAttempts++;
        this.lastFailedAt = Instant.now();
        this.lastFailureReason = reason;
    }
}