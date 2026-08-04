package com.adrovis.adrovis_backend.contact.service;

import com.adrovis.adrovis_backend.contact.dto.request.CallbackRequest;
import com.adrovis.adrovis_backend.contact.dto.request.ConsultationRequest;
import com.adrovis.adrovis_backend.contact.dto.response.LeadResponse;

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

}