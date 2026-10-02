package com.adrovis.adrovis_backend.unpaidinternship.controller;

import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import com.adrovis.adrovis_backend.unpaidinternship.dto.response.UnpaidInternshipApplicationResponse;
import com.adrovis.adrovis_backend.unpaidinternship.enums.UnpaidInternshipApplicationStatus;
import com.adrovis.adrovis_backend.unpaidinternship.service.UnpaidInternshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/unpaid-internship")
@RequiredArgsConstructor
public class AdminUnpaidInternshipController {

    private final UnpaidInternshipService service;

    @GetMapping("/applications")
    public ResponseEntity<
            ApiResponse<Page<UnpaidInternshipApplicationResponse>>
            > getApplications(

            @RequestParam(required = false)
            UnpaidInternshipApplicationStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size

    ) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable =
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Unpaid internship applications retrieved successfully.",
                        service.getApplications(
                                status,
                                pageable
                        )
                )
        );
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<
            ApiResponse<UnpaidInternshipApplicationResponse>
            > getApplication(
            @PathVariable String applicationId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Application retrieved successfully.",
                        service.getApplication(applicationId)
                )
        );
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<
            ApiResponse<UnpaidInternshipApplicationResponse>
            > updateStatus(

            @PathVariable String applicationId,

            @RequestParam
            UnpaidInternshipApplicationStatus status

    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Application status updated successfully.",
                        service.updateStatus(
                                applicationId,
                                status
                        )
                )
        );
    }

    @GetMapping("/applications/{applicationId}/resume")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable String applicationId
    ) {

        Resource resource =
                service.downloadResume(applicationId);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + resource.getFilename()
                                + "\""
                )
                .body(resource);
    }
}