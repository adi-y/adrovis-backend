package com.adrovis.adrovis_backend.career.service;

import com.adrovis.adrovis_backend.career.dto.request.CreateApplicationRequest;
import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationCreateRequest;
import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationSubmitRequest;
import com.adrovis.adrovis_backend.career.dto.request.UpdateApplicationStatusRequest;
import com.adrovis.adrovis_backend.career.dto.response.ApplicationCreatedResponse;
import com.adrovis.adrovis_backend.career.dto.response.ApplicationResponse;
import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {

    ApplicationResponse createApplication(
            CreateApplicationRequest request,
            MultipartFile resume
    );

    ApplicationResponse getApplicationById(UUID applicationId);

    List<ApplicationResponse> getApplicationsByJob(UUID jobId);

    List<ApplicationResponse> getApplicationsByStatus(
            ApplicationStatus status
    );

    ApplicationResponse updateApplicationStatus(
            String applicationId,
            UpdateApplicationStatusRequest request
    );


}