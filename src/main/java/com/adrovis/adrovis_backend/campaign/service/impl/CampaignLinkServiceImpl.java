package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.config.CampaignProperties;
import com.adrovis.adrovis_backend.campaign.service.CampaignLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignLinkServiceImpl implements CampaignLinkService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final CampaignProperties properties;

    @Override
    public String generateContinuationToken(
            String applicationId,
            String email
    ) {
        long expiresAt =
                Instant.now()
                        .plusSeconds(
                                properties.getContinuationValidityHours()
                                        * 3600
                        )
                        .getEpochSecond();

        String payload =
                "CONTINUE|" +
                        applicationId.trim() +
                        "|" +
                        email.trim().toLowerCase() +
                        "|" +
                        expiresAt;

        return sign(payload);
    }

    @Override
    public boolean validateContinuationToken(
            String token,
            String applicationId,
            String email
    ) {
        try {
            String payload = verify(token);

            String[] parts = payload.split("\\|", -1);

            if (parts.length != 4) {
                return false;
            }

            if (!"CONTINUE".equals(parts[0])) {
                return false;
            }

            if (!applicationId.equals(parts[1])) {
                return false;
            }

            if (!email.trim().equalsIgnoreCase(parts[2])) {
                return false;
            }

            long expiresAt = Long.parseLong(parts[3]);

            return Instant.now().getEpochSecond() <= expiresAt;

        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public String generateUnsubscribeToken(UUID candidateId) {
        long expiresAt =
                Instant.now()
                        .plusSeconds(
                                properties.getContinuationValidityHours()
                                        * 3600
                        )
                        .getEpochSecond();

        String payload =
                "UNSUBSCRIBE|" +
                        candidateId +
                        "|" +
                        expiresAt;

        return sign(payload);
    }

    @Override
    public UUID validateUnsubscribeToken(String token) {
        String payload = verify(token);
        String[] parts = payload.split("\\|", -1);

        if (parts.length != 3 || !"UNSUBSCRIBE".equals(parts[0])) {
            throw new IllegalArgumentException(
                    "Invalid unsubscribe token."
            );
        }

        long expiresAt = Long.parseLong(parts[2]);

        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new IllegalArgumentException(
                    "Unsubscribe token has expired."
            );
        }

        return UUID.fromString(parts[1]);
    }

    @Override
    public String generateTrackingToken(
            UUID campaignEmailId,
            String event
    ) {
        long expiresAt =
                Instant.now()
                        .plusSeconds(
                                properties.getContinuationValidityHours()
                                        * 3600
                        )
                        .getEpochSecond();

        String payload =
                "TRACK|" +
                        campaignEmailId +
                        "|" +
                        event +
                        "|" +
                        expiresAt;

        return sign(payload);
    }

    @Override
    public UUID validateTrackingToken(
            String token,
            String event
    ) {
        String payload = verify(token);
        String[] parts = payload.split("\\|", -1);

        if (parts.length != 4 || !"TRACK".equals(parts[0])) {
            throw new IllegalArgumentException(
                    "Invalid tracking token."
            );
        }

        if (!event.equals(parts[2])) {
            throw new IllegalArgumentException(
                    "Tracking event mismatch."
            );
        }

        long expiresAt = Long.parseLong(parts[3]);

        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new IllegalArgumentException(
                    "Tracking token has expired."
            );
        }

        return UUID.fromString(parts[1]);
    }

    private String sign(String payload) {
        byte[] payloadBytes =
                payload.getBytes(StandardCharsets.UTF_8);

        String encodedPayload =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(payloadBytes);

        String signature =
                hmac(encodedPayload);

        return encodedPayload + "." + signature;
    }

    private String verify(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Token is required."
            );
        }

        String[] parts = token.split("\\.", -1);

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid token."
            );
        }

        String expectedSignature = hmac(parts[0]);

        if (!constantTimeEquals(
                expectedSignature,
                parts[1]
        )) {
            throw new IllegalArgumentException(
                    "Invalid token signature."
            );
        }

        return new String(
                Base64.getUrlDecoder().decode(parts[0]),
                StandardCharsets.UTF_8
        );
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            SecretKeySpec key =
                    new SecretKeySpec(
                            getSecret().getBytes(StandardCharsets.UTF_8),
                            HMAC_ALGORITHM
                    );

            mac.init(key);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            mac.doFinal(
                                    value.getBytes(StandardCharsets.UTF_8)
                            )
                    );

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to generate campaign token.",
                    ex
            );
        }
    }

    private String getSecret() {
        String secret = properties.getContinuationSecret();

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Campaign continuation secret is not configured."
            );
        }

        return secret;
    }

    private boolean constantTimeEquals(
            String first,
            String second
    ) {
        byte[] a =
                first.getBytes(StandardCharsets.UTF_8);

        byte[] b =
                second.getBytes(StandardCharsets.UTF_8);

        return java.security.MessageDigest.isEqual(a, b);
    }
}