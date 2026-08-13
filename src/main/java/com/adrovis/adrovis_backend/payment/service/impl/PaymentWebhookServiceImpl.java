package com.adrovis.adrovis_backend.payment.service.impl;

import com.adrovis.adrovis_backend.email.service.EmailService;
import com.adrovis.adrovis_backend.payment.entity.PaymentTransaction;
import com.adrovis.adrovis_backend.payment.entity.PaymentWebhookEvent;
import com.adrovis.adrovis_backend.payment.repository.PaymentTransactionRepository;
import com.adrovis.adrovis_backend.payment.repository.PaymentWebhookEventRepository;
import com.adrovis.adrovis_backend.payment.service.PaymentWebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookServiceImpl
        implements PaymentWebhookService {

    private final PaymentWebhookEventRepository eventRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public void process(
            String eventId,
            String eventType,
            String payload
    ) {

        if (eventId == null || eventId.isBlank()) {

            throw new IllegalArgumentException(
                    "Missing Razorpay event id."
            );
        }

        /*
         * Atomic idempotency protection.
         */
        try {

            if (eventRepository.existsByEventId(eventId)) {
                log.info(
                        "Ignoring duplicate Razorpay webhook. eventId={}",
                        eventId
                );
                return;
            }

            eventRepository.saveAndFlush(
                    new PaymentWebhookEvent(
                            eventId,
                            eventType
                    )
            );

        } catch (DataIntegrityViolationException ex) {

            log.info(
                    "Razorpay webhook already processed concurrently. eventId={}",
                    eventId
            );

            return;
        }

        try {

            JsonNode root =
                    objectMapper.readTree(payload);

            switch (eventType) {

                case "payment_link.paid" ->
                        handlePaid(root);

                case "payment_link.expired" ->
                        handleExpired(root);

                case "payment_link.cancelled" ->
                        handleCancelled(root);

                case "payment.failed" ->
                        handlePaymentFailed(root);

                default ->
                        log.info(
                                "Ignoring unsupported Razorpay event: {}",
                                eventType
                        );
            }

        } catch (Exception ex) {

            log.error(
                    "Failed processing Razorpay webhook. eventId={}, eventType={}",
                    eventId,
                    eventType,
                    ex
            );

            throw new IllegalStateException(
                    "Unable to process Razorpay webhook.",
                    ex
            );
        }
    }

    private void handlePaid(
            JsonNode root
    ) {

        JsonNode paymentLink =
                root.path("payload")
                        .path("payment_link")
                        .path("entity");

        String paymentLinkId =
                text(paymentLink, "id");

        String paymentId =
                root.path("payload")
                        .path("payment")
                        .path("entity")
                        .path("id")
                        .asText(null);

        PaymentTransaction payment =
                paymentRepository
                        .findByRazorpayPaymentLinkId(
                                paymentLinkId
                        )
                        .orElse(null);

        if (payment == null) {

            log.error(
                    "Payment link not found. razorpayPaymentLinkId={}",
                    paymentLinkId
            );

            return;
        }

        /*
         * Never trust the webhook amount blindly.
         * Compare Razorpay amount with our stored amount.
         */
        long razorpayAmount =
                paymentLink.path("amount").asLong(-1);

        if (razorpayAmount != payment.getAmount()) {

            log.error(
                    "Payment amount mismatch. expected={}, actual={}, link={}",
                    payment.getAmount(),
                    razorpayAmount,
                    paymentLinkId
            );

            throw new IllegalStateException(
                    "Payment amount mismatch."
            );
        }

        payment.markPaid(
                paymentId,
                Instant.now()
        );

        emailService.sendPaymentSuccessEmailAsync(
                payment.getApplication(),
                payment
        );
    }

    private void handleExpired(
            JsonNode root
    ) {

        String paymentLinkId =
                root.path("payload")
                        .path("payment_link")
                        .path("entity")
                        .path("id")
                        .asText(null);

        PaymentTransaction payment =
                paymentRepository
                        .findByRazorpayPaymentLinkId(
                                paymentLinkId
                        )
                        .orElse(null);

        if (payment != null) {
            payment.markExpired();
        }
    }

    private void handleCancelled(
            JsonNode root
    ) {

        String paymentLinkId =
                root.path("payload")
                        .path("payment_link")
                        .path("entity")
                        .path("id")
                        .asText(null);

        PaymentTransaction payment =
                paymentRepository
                        .findByRazorpayPaymentLinkId(
                                paymentLinkId
                        )
                        .orElse(null);

        if (payment != null) {
            payment.markCancelled();
        }
    }

    private void handlePaymentFailed(
            JsonNode root
    ) {

        JsonNode paymentNode =
                root.path("payload")
                        .path("payment")
                        .path("entity");

        String orderId =
                paymentNode.path("order_id")
                        .asText(null);

        /*
         * payment.failed does not always contain the
         * Payment Link ID. Razorpay can also emit a
         * failed attempt before a later successful attempt.
         *
         * Therefore we record it only when we can safely
         * correlate it through our stored Razorpay order.
         */
        if (orderId == null) {

            log.warn(
                    "payment.failed received without order_id."
            );

            return;
        }

        PaymentTransaction payment =
                paymentRepository
                        .findAll()
                        .stream()
                        .filter(p ->
                                orderId.equals(
                                        p.getRazorpayOrderId()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        if (payment == null) {

            log.warn(
                    "Unable to correlate failed payment. orderId={}",
                    orderId
            );

            return;
        }

        payment.recordFailedAttempt(
                paymentNode
                        .path("error_description")
                        .asText("Payment failed")
        );
    }

    private String text(
            JsonNode node,
            String field
    ) {

        return node.path(field)
                .asText(null);
    }
}