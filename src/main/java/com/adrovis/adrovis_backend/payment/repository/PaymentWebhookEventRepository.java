package com.adrovis.adrovis_backend.payment.repository;

import com.adrovis.adrovis_backend.payment.entity.PaymentWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentWebhookEventRepository
        extends JpaRepository<PaymentWebhookEvent, UUID> {

    boolean existsByEventId(String eventId);
}