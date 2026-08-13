package com.adrovis.adrovis_backend.payment.controller;

import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import com.adrovis.adrovis_backend.payment.dto.response.PaymentResponse;
import com.adrovis.adrovis_backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/applications/{applicationId}/link")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPaymentLink(
            @PathVariable UUID applicationId
    ) {

        PaymentResponse response =
                paymentService.createPaymentLink(applicationId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Payment link created successfully.",
                        response
                )
        );
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID applicationId
    ) {

        PaymentResponse response =
                paymentService.getPayment(applicationId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Payment details fetched successfully.",
                        response
                )
        );
    }
}