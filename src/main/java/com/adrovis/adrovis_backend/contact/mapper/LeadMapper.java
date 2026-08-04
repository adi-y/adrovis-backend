package com.adrovis.adrovis_backend.contact.mapper;

import com.adrovis.adrovis_backend.contact.dto.request.CallbackRequest;
import com.adrovis.adrovis_backend.contact.dto.request.ConsultationRequest;
import com.adrovis.adrovis_backend.contact.dto.response.LeadResponse;
import com.adrovis.adrovis_backend.contact.entity.Lead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Maps Contact module DTOs to entities and vice versa.
 *
 * <p>
 * Responsible only for object transformation.
 * Business rules such as lead type assignment, status initialization,
 * persistence and notifications belong to the service layer.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface LeadMapper {

    /**
     * Maps a callback request into a Lead entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "leadType", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "projectType", ignore = true)
    @Mapping(target = "budget", ignore = true)
    @Mapping(target = "message", ignore = true)
    Lead toCallbackLead(CallbackRequest request);

    /**
     * Maps a consultation request into a Lead entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "leadType", ignore = true)
    Lead toConsultationLead(ConsultationRequest request);

    /**
     * Maps a Lead entity into a response DTO.
     */
    LeadResponse toResponse(Lead lead);

    /**
     * Updates an existing consultation lead.
     * Reserved for future Admin functionality.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "leadType", ignore = true)
    void updateConsultationLead(
            ConsultationRequest request,
            @MappingTarget Lead lead
    );
}