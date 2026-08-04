package com.adrovis.adrovis_backend.contact.repository;

import com.adrovis.adrovis_backend.contact.entity.Lead;
import com.adrovis.adrovis_backend.contact.enums.LeadStatus;
import com.adrovis.adrovis_backend.contact.enums.LeadType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for managing {@link Lead} persistence.
 *
 * <p>
 * Public Contact APIs currently only create leads.
 * Additional query methods are provided to support the
 * future Admin CRM module without requiring repository changes.
 * </p>
 */
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    /**
     * Returns paginated leads filtered by type and status.
     *
     * <p>
     * Intended for the future Admin CRM dashboard.
     * </p>
     *
     * @param leadType lead category
     * @param status lifecycle status
     * @param pageable pagination information
     * @return paginated leads
     */
    Page<Lead> findByLeadTypeAndStatus(
            LeadType leadType,
            LeadStatus status,
            Pageable pageable
    );

}