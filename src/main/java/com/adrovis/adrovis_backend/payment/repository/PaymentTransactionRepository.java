package com.adrovis.adrovis_backend.payment.repository;

import com.adrovis.adrovis_backend.payment.entity.PaymentTransaction;
import com.adrovis.adrovis_backend.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction>
    findByApplication_Id(UUID applicationId);

    Optional<PaymentTransaction>
    findByRazorpayPaymentLinkId(String paymentLinkId);

    Optional<PaymentTransaction>
    findByReferenceId(String referenceId);

    boolean existsByApplication_IdAndStatus(
            UUID applicationId,
            PaymentStatus status
    );
}