package com.adrovis.adrovis_backend.payment.service.impl;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.common.exception.ResourceNotFoundException;
import com.adrovis.adrovis_backend.email.service.EmailService;
import com.adrovis.adrovis_backend.payment.client.RazorpayClient;
import com.adrovis.adrovis_backend.payment.config.PaymentProperties;
import com.adrovis.adrovis_backend.payment.dto.response.PaymentResponse;
import com.adrovis.adrovis_backend.payment.entity.PaymentTransaction;
import com.adrovis.adrovis_backend.payment.enums.PaymentStatus;
import com.adrovis.adrovis_backend.payment.repository.PaymentTransactionRepository;
import com.adrovis.adrovis_backend.payment.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final ApplicationRepository applicationRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final PaymentProperties paymentProperties;
    private final EmailService emailService;

    @Override
    @Transactional
    public PaymentResponse createPaymentLink(UUID applicationId) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found with id: "
                                                + applicationId
                                )
                        );

        /*
         * Never create another payment link when the candidate
         * already has a usable or completed payment.
         */
        PaymentTransaction existing =
                paymentRepository
                        .findByApplication_Id(applicationId)
                        .orElse(null);

        if (existing != null) {

            if (existing.getStatus() == PaymentStatus.PAID) {
                return toResponse(existing);
            }

            if (existing.getStatus() == PaymentStatus.ISSUED) {
                return toResponse(existing);
            }
        }

        /*
         * Payment can only be generated after the application
         * has been shortlisted.
         */
        if (application.getApplicationStatus()
                != ApplicationStatus.SHORTLISTED) {

            throw new IllegalStateException(
                    "Payment can only be created for a shortlisted application."
            );
        }

        String referenceId =
                "ADV-" + application.getApplicationId();

        long expireBy =
                Instant.now()
                        .plusSeconds(
                                paymentProperties.getExpiryHours() * 3600
                        )
                        .getEpochSecond();

        /*
         * Create the Payment Link at Razorpay.
         *
         * Razorpay customer details are still passed to Razorpay,
         * but Razorpay's own email/SMS notification is disabled
         * inside RazorpayClient.
         *
         * Adrovis sends the branded email through Resend instead.
         */
        JsonNode response =
                razorpayClient.createPaymentLink(
                        paymentProperties.getAmountInr() * 100,
                        paymentProperties.getCurrency(),
                        referenceId,
                        "Internship Onboarding - "
                                + application.getJobTitleSnapshot(),
                        application.getApplicantName(),
                        application.getApplicantEmail(),
                        application.getApplicantPhone(),
                        expireBy
                );

        PaymentTransaction payment =
                new PaymentTransaction(
                        application,
                        referenceId,
                        response.get("id").asText(),
                        getNullable(response, "order_id"),
                        paymentProperties.getAmountInr() * 100,
                        paymentProperties.getCurrency(),
                        response.get("short_url").asText()
                );

        PaymentTransaction savedPayment =
                paymentRepository.save(payment);

        /*
         * Send the official Adrovis payment email.
         *
         * This is asynchronous, so an email provider failure
         * does not roll back the successfully created payment link.
         */
        emailService.sendPaymentLinkEmailAsync(
                application,
                savedPayment
        );

        return toResponse(savedPayment);
    }

    @Override
    public PaymentResponse getPayment(UUID applicationId) {

        PaymentTransaction payment =
                paymentRepository
                        .findByApplication_Id(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found for application."
                                )
                        );

        return toResponse(payment);
    }

    private PaymentResponse toResponse(
            PaymentTransaction payment
    ) {

        return new PaymentResponse(
                payment.getApplication().getApplicationId(),
                payment.getReferenceId(),
                payment.getRazorpayPaymentLinkId(),
                payment.getPaymentLinkUrl(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getPaidAt(),
                payment.getFailedAttempts()
        );
    }

    private String getNullable(
            JsonNode node,
            String field
    ) {

        JsonNode value = node.get(field);

        return value == null || value.isNull()
                ? null
                : value.asText();
    }
}