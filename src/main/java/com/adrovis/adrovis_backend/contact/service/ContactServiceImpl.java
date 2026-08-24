package com.adrovis.adrovis_backend.contact.service.impl;

import com.adrovis.adrovis_backend.contact.dto.request.CallbackRequest;
import com.adrovis.adrovis_backend.contact.dto.request.ConsultationRequest;
import com.adrovis.adrovis_backend.contact.dto.response.LeadResponse;
import com.adrovis.adrovis_backend.contact.entity.Lead;
import com.adrovis.adrovis_backend.contact.enums.LeadStatus;
import com.adrovis.adrovis_backend.contact.enums.LeadType;
import com.adrovis.adrovis_backend.contact.mapper.LeadMapper;
import com.adrovis.adrovis_backend.contact.repository.LeadRepository;
import com.adrovis.adrovis_backend.contact.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactServiceImpl implements ContactService {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;

    @Override
    @Transactional
    public LeadResponse createCallbackLead(CallbackRequest request) {
        Lead lead = leadMapper.toCallbackLead(request);
        lead.initialize(LeadType.CALLBACK);
        Lead savedLead = leadRepository.save(lead);
        return leadMapper.toResponse(savedLead);
    }

    @Override
    @Transactional
    public LeadResponse createConsultationLead(ConsultationRequest request) {
        Lead lead = leadMapper.toConsultationLead(request);
        lead.initialize(LeadType.CONSULTATION);
        Lead savedLead = leadRepository.save(lead);
        return leadMapper.toResponse(savedLead);
    }

    @Override
    public Page<LeadResponse> getLeads(
            LeadType leadType,
            LeadStatus status,
            Pageable pageable
    ) {
        Page<Lead> leads;

        if (leadType != null && status != null) {
            leads = leadRepository.findByLeadTypeAndStatus(leadType, status, pageable);
        } else if (leadType != null) {
            leads = leadRepository.findByLeadType(leadType, pageable);
        } else if (status != null) {
            leads = leadRepository.findByStatus(status, pageable);
        } else {
            leads = leadRepository.findAll(pageable);
        }

        return leads.map(leadMapper::toResponse);
    }
}
