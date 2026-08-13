package com.adrovis.adrovis_backend.payment.security;

import com.adrovis.adrovis_backend.payment.config.RazorpayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
public class RazorpayWebhookVerifier {

    private final RazorpayProperties properties;

    public boolean verify(
            String payload,
            String signature
    ) {

        if (signature == null || signature.isBlank()) {
            return false;
        }

        try {

            Mac mac =
                    Mac.getInstance("HmacSHA256");

            mac.init(
                    new SecretKeySpec(
                            properties.getWebhookSecret()
                                    .getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    )
            );

            byte[] digest =
                    mac.doFinal(
                            payload.getBytes(StandardCharsets.UTF_8)
                    );

            String expected =
                    bytesToHex(digest);

            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception ex) {

            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {

        StringBuilder result =
                new StringBuilder();

        for (byte b : bytes) {
            result.append(
                    String.format("%02x", b)
            );
        }

        return result.toString();
    }
}