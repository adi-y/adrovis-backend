package com.adrovis.adrovis_backend.unpaidinternship.service;

import com.adrovis.adrovis_backend.unpaidinternship.dto.request.UnpaidInternshipApplicationRequest;
import com.adrovis.adrovis_backend.unpaidinternship.dto.response.UnpaidInternshipApplicationResponse;
import com.adrovis.adrovis_backend.unpaidinternship.dto.response.UnpaidInternshipDetailsResponse;
import com.adrovis.adrovis_backend.unpaidinternship.enums.UnpaidInternshipApplicationStatus;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UnpaidInternshipService {

    UnpaidInternshipDetailsResponse getInternshipDetails();

    UnpaidInternshipApplicationResponse submitApplication(
            UnpaidInternshipApplicationRequest request
    );

    Page<UnpaidInternshipApplicationResponse> getApplications(
            UnpaidInternshipApplicationStatus status,
            Pageable pageable
    );

    UnpaidInternshipApplicationResponse getApplication(
            String applicationId
    );

    UnpaidInternshipApplicationResponse updateStatus(
            String applicationId,
            UnpaidInternshipApplicationStatus status
    );

    Resource downloadResume(String applicationId);
}