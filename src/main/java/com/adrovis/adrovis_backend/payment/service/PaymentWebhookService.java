package com.adrovis.adrovis_backend.payment.service;

public interface PaymentWebhookService {

    void process(
            String eventId,
            String eventType,
            String payload
    );
}