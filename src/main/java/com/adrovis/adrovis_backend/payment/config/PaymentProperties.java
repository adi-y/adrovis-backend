package com.adrovis.adrovis_backend.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private long amountInr = 2000;
    private String currency = "INR";
    private long expiryHours = 72;

    private String frontendPaymentSuccessUrl;
    private String frontendPaymentFailureUrl;
}