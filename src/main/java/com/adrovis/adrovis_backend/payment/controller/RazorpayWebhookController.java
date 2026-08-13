package com.adrovis.adrovis_backend.payment.controller;

import com.adrovis.adrovis_backend.payment.security.RazorpayWebhookVerifier;
import com.adrovis.adrovis_backend.payment.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class RazorpayWebhookController {

    private final RazorpayWebhookVerifier verifier;
    private final PaymentWebhookService webhookService;

    @PostMapping("/razorpay")
    public ResponseEntity<Void> handleRazorpayWebhook(
            @RequestHeader(
                    value = "X-Razorpay-Signature",
                    required = false
            )
            String signature,

            @RequestHeader(
                    value = "x-razorpay-event-id",
                    required = false
            )
            String eventId,

            @RequestBody String rawBody
    ) {

        if (!verifier.verify(rawBody, signature)) {

            log.warn(
                    "Invalid Razorpay webhook signature."
            );

            return ResponseEntity
                    .status(400)
                    .build();
        }

        /*
         * Event type is read AFTER signature verification.
         */
        String eventType =
                extractEventType(rawBody);

        webhookService.process(
                eventId,
                eventType,
                rawBody
        );

        return ResponseEntity.ok().build();
    }

    private String extractEventType(
            String rawBody
    ) {

        try {

            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();

            return mapper.readTree(rawBody)
                    .path("event")
                    .asText();

        } catch (Exception ex) {

            throw new IllegalArgumentException(
                    "Invalid Razorpay webhook payload."
            );
        }
    }
}