package com.adrovis.adrovis_backend.career.service;

import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationCreateRequest;
import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationSubmitRequest;
import com.adrovis.adrovis_backend.career.dto.response.ApplicationCreatedResponse;
import com.adrovis.adrovis_backend.career.dto.response.ProgramApplicationContinuationResponse;

public interface ProgramApplicationService {

    /**
     * Creates a draft (PENDING) program application.
     */
    ApplicationCreatedResponse createDraft(
            ProgramApplicationCreateRequest request
    );

    /**
     * Submits an existing draft application.
     */
    void submit(
            String applicationId,
            ProgramApplicationSubmitRequest request
    );

    ProgramApplicationContinuationResponse getContinuation(
            String applicationId,
            String token
    );

    void submitContinuation(
            String applicationId,
            String token,
            ProgramApplicationSubmitRequest request
    );
}