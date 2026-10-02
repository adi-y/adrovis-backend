package com.adrovis.adrovis_backend.unpaidinternship.dto.response;

public record UnpaidInternshipDetailsResponse(

        String title,

        String description,

        String batch,

        boolean acceptingApplications

) {
}