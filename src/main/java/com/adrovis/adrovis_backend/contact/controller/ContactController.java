package com.adrovis.adrovis_backend.contact.controller;

import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import com.adrovis.adrovis_backend.contact.dto.request.CallbackRequest;
import com.adrovis.adrovis_backend.contact.dto.request.ConsultationRequest;
import com.adrovis.adrovis_backend.contact.dto.response.LeadResponse;
import com.adrovis.adrovis_backend.contact.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import com.adrovis.adrovis_backend.contact.enums.LeadStatus;
import com.adrovis.adrovis_backend.contact.enums.LeadType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;


@RestController
@RequestMapping("/api/v1/admin/contact")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Contact",
        description = "Public APIs for callback requests and consultation enquiries."
)
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/callback")
    @Operation(
            summary = "Request a callback",
            description = """
                    Creates a callback lead.
                    A sales representative will contact the visitor using
                    the provided phone number.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Callback request submitted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<LeadResponse>> requestCallback(

            @Valid
            @RequestBody CallbackRequest request
    ) {

        LeadResponse response =
                contactService.createCallbackLead(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED,
                                "We've received your request. Our team will call you shortly.",
                                response
                        )
                );
    }

    @PostMapping("/consultation")
    @Operation(
            summary = "Schedule a consultation",
            description = """
                    Creates a consultation lead.
                    The submitted business details are stored and
                    our team will contact the visitor to schedule
                    a consultation.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Consultation request submitted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<LeadResponse>> scheduleConsultation(

            @Valid
            @RequestBody ConsultationRequest request
    ) {

        LeadResponse response =
                contactService.createConsultationLead(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED,
                                "Thanks! We'll be in touch to schedule your consultation.",
                                response
                        )
                );
    }

    @GetMapping("/leads")
    @Operation(
            summary = "Get contact leads",
            description = "Retrieves paginated contact leads for administrative use."
    )
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> getLeads(

            @RequestParam(required = false)
            LeadType leadType,

            @RequestParam(required = false)
            LeadStatus status,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<LeadResponse> leads =
                contactService.getLeads(
                        leadType,
                        status,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Leads retrieved successfully.",
                        leads
                )
        );
    }

}