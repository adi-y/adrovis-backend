package com.adrovis.adrovis_backend.payment.client;

import com.adrovis.adrovis_backend.payment.config.RazorpayProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RazorpayClient {

    private final RazorpayProperties properties;
    private final ObjectMapper objectMapper;

    private RestClient client() {

        String credentials =
                properties.getKeyId()
                        + ":"
                        + properties.getKeySecret();

        String encoded =
                Base64.getEncoder()
                        .encodeToString(
                                credentials.getBytes(StandardCharsets.UTF_8)
                        );

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Basic " + encoded
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    public JsonNode createPaymentLink(
            long amount,
            String currency,
            String referenceId,
            String description,
            String name,
            String email,
            String phone,
            long expireBy
    ) {

        Map<String, Object> payload = Map.of(
                "amount", amount,
                "currency", currency,
                "accept_partial", false,
                "reference_id", referenceId,
                "description", description,
                "expire_by", expireBy,

                "customer", Map.of(
                        "name", name,
                        "email", email,
                        "contact", phone == null ? "" : phone
                ),

                "notify", Map.of(
                        "email", false,
                        "sms", true
                ),

                "reminder_enable", false
        );

        String response =
                client()
                        .post()
                        .uri("/v1/payment_links")
                        .body(payload)
                        .retrieve()
                        .body(String.class);

        try {
            return objectMapper.readTree(response);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Invalid response received from Razorpay.",
                    ex
            );
        }
    }
}