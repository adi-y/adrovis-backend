package com.adrovis.adrovis_backend.payment.service;

import com.adrovis.adrovis_backend.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPaymentLink(UUID applicationId);

    PaymentResponse getPayment(UUID applicationId);
}