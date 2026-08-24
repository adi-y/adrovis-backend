package com.adrovis.adrovis_backend.contact.repository;

import com.adrovis.adrovis_backend.contact.entity.Lead;
import com.adrovis.adrovis_backend.contact.enums.LeadStatus;
import com.adrovis.adrovis_backend.contact.enums.LeadType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Page<Lead> findByLeadTypeAndStatus(LeadType leadType, LeadStatus status, Pageable pageable);

    Page<Lead> findByLeadType(LeadType leadType, Pageable pageable);

    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);
}
