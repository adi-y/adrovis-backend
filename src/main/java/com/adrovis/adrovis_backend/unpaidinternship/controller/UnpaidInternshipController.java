package com.adrovis.adrovis_backend.unpaidinternship.controller;

import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import com.adrovis.adrovis_backend.unpaidinternship.dto.request.UnpaidInternshipApplicationRequest;
import com.adrovis.adrovis_backend.unpaidinternship.dto.response.UnpaidInternshipApplicationResponse;
import com.adrovis.adrovis_backend.unpaidinternship.dto.response.UnpaidInternshipDetailsResponse;
import com.adrovis.adrovis_backend.unpaidinternship.service.UnpaidInternshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/unpaid-internship")
@RequiredArgsConstructor
public class UnpaidInternshipController {

    private final UnpaidInternshipService service;

    @GetMapping
    public ResponseEntity<
            ApiResponse<UnpaidInternshipDetailsResponse>
            > getDetails() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Unpaid internship details retrieved successfully.",
                        service.getInternshipDetails()
                )
        );
    }

    @PostMapping(
            value = "/applications",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<
            ApiResponse<UnpaidInternshipApplicationResponse>
            > submitApplication(

            @Valid
            @ModelAttribute
            UnpaidInternshipApplicationRequest request

    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED,
                                "Unpaid internship application submitted successfully.",
                                service.submitApplication(request)
                        )
                );
    }
}