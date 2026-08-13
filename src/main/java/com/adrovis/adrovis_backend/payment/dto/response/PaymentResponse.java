package com.adrovis.adrovis_backend.payment.dto.response;

import com.adrovis.adrovis_backend.payment.enums.PaymentStatus;

import java.time.Instant;

public record PaymentResponse(
        String applicationId,
        String referenceId,
        String paymentLinkId,
        String paymentLinkUrl,
        long amount,
        String currency,
        PaymentStatus status,
        Instant createdAt,
        Instant paidAt,
        int failedAttempts
) {
}