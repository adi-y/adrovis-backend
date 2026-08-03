package com.adrovis.adrovis_backend.career.controller;

import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationCreateRequest;
import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationSubmitRequest;
import com.adrovis.adrovis_backend.career.dto.response.ApplicationCreatedResponse;
import com.adrovis.adrovis_backend.career.service.ProgramApplicationService;
import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/careers/program/applications")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Program Applications",
        description = "APIs for the Associate Software Engineer Program application workflow."
)
public class ProgramApplicationController {

    private final ProgramApplicationService programApplicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create program application draft",
            description = """
                    Creates a draft application for the Associate Software Engineer Program.
                    The uploaded resume is stored and the application is saved with
                    PENDING status until the applicant completes the final submission.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Application draft created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "Uploaded file exceeds maximum allowed size",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "415",
                    description = "Unsupported file type",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<ApplicationCreatedResponse>> createDraft(
            @Valid @ModelAttribute ProgramApplicationCreateRequest request
    ) {

        ApplicationCreatedResponse result =
                programApplicationService.createDraft(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED,
                        "Application draft created successfully.",
                        result
                ));
    }

    @PatchMapping("/{applicationId}/submit")
    @Operation(
            summary = "Submit program application",
            description = """
                    Submits a previously created draft application after the
                    applicant accepts the terms and conditions.
                    Once submitted, the application status changes from
                    PENDING to SUBMITTED and a confirmation email is sent.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Application submitted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Application not found",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Application has already been submitted",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<Void>> submitApplication(

            @Parameter(
                    description = "Human-readable application ID (Example: APP202600001)",
                    required = true,
                    example = "APP202600001"
            )
            @PathVariable String applicationId,

            @Valid
            @RequestBody ProgramApplicationSubmitRequest request
    ) {

        programApplicationService.submit(applicationId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Application submitted successfully.",
                        null
                )
        );
    }
}