package com.adrovis.adrovis_backend.contact.service;

import com.adrovis.adrovis_backend.contact.dto.request.CallbackRequest;
import com.adrovis.adrovis_backend.contact.dto.request.ConsultationRequest;
import com.adrovis.adrovis_backend.contact.dto.response.LeadResponse;

import com.adrovis.adrovis_backend.contact.enums.LeadStatus;
import com.adrovis.adrovis_backend.contact.enums.LeadType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
/**
 * Service contract for handling public contact requests.
 */
public interface ContactService {

    /**
     * Creates a callback lead.
     *
     * @param request callback request payload
     * @return created lead information
     */
    LeadResponse createCallbackLead(CallbackRequest request);

    /**
     * Creates a consultation lead.
     *
     * @param request consultation request payload
     * @return created lead information
     */
    LeadResponse createConsultationLead(ConsultationRequest request);

    Page<LeadResponse> getLeads(
            LeadType leadType,
            LeadStatus status,
            Pageable pageable
    );
}