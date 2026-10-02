package com.adrovis.adrovis_backend.unpaidinternship.mapper;

import com.adrovis.adrovis_backend.unpaidinternship.dto.response.UnpaidInternshipApplicationResponse;
import com.adrovis.adrovis_backend.unpaidinternship.entity.UnpaidInternshipApplication;
import org.springframework.stereotype.Component;

@Component
public class UnpaidInternshipApplicationMapper {

    public UnpaidInternshipApplicationResponse toResponse(
            UnpaidInternshipApplication application
    ) {
        return new UnpaidInternshipApplicationResponse(
                application.getApplicationId(),
                application.getFullName(),
                application.getEmail(),
                application.getPhone(),
                application.getCollege(),
                application.getGraduationYear(),
                application.getBatch(),
                application.getInternshipTitle(),
                application.getStatus(),
                application.getResumeOriginalName(),
                application.getResumeMimeType(),
                application.getResumeSizeBytes(),
                application.getSubmittedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}