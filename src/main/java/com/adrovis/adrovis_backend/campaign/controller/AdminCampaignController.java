package com.adrovis.adrovis_backend.campaign.controller;

import com.adrovis.adrovis_backend.campaign.dto.request.CreateCampaignRequest;
import com.adrovis.adrovis_backend.campaign.dto.response.CampaignEmailResponse;
import com.adrovis.adrovis_backend.campaign.dto.response.CampaignRecipientAnalyticsResponse;
import com.adrovis.adrovis_backend.campaign.dto.response.CampaignResponse;
import com.adrovis.adrovis_backend.campaign.entity.Campaign;
import com.adrovis.adrovis_backend.campaign.repository.CampaignEmailRepository;
import com.adrovis.adrovis_backend.campaign.repository.CampaignRecipientRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignAnalyticsService;
import com.adrovis.adrovis_backend.campaign.service.CampaignDispatchService;
import com.adrovis.adrovis_backend.campaign.service.CampaignService;
import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import com.adrovis.adrovis_backend.campaign.enums.CampaignStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/campaigns")
public class AdminCampaignController {

    private final CampaignService campaignService;
    private final CampaignDispatchService dispatchService;
    private final CampaignEmailRepository campaignEmailRepository;
    private final CampaignRecipientRepository campaignRecipientRepository;
    private final CampaignAnalyticsService analyticsService;

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignResponse>> create(
            @Valid
            @RequestBody
            CreateCampaignRequest request
    ) {
        Campaign campaign =
                campaignService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED,
                                "Campaign created successfully.",
                                CampaignResponse.from(campaign)
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<CampaignResponse>>
            > getAll() {

        List<CampaignResponse> campaigns =
                campaignService.findAll()
                        .stream()
                        .map(CampaignResponse::from)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Campaigns retrieved successfully.",
                        campaigns
                )
        );
    }

    @GetMapping("/{campaignId}")
    public ResponseEntity<ApiResponse<CampaignResponse>> get(
            @PathVariable UUID campaignId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Campaign retrieved successfully.",
                        CampaignResponse.from(
                                campaignService.get(campaignId)
                        )
                )
        );
    }

    @PostMapping("/{campaignId}/run")
    public ResponseEntity<ApiResponse<Void>> run(
            @PathVariable UUID campaignId
    ) {

        Campaign campaign =
                campaignService.get(campaignId);

        dispatchService.dispatchNextDueWeek(campaign);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Campaign dispatch processed successfully.",
                        null
                )
        );
    }

    @PostMapping("/{campaignId}/close")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable UUID campaignId
    ) {

        campaignService.close(campaignId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Campaign closed successfully.",
                        null
                )
        );
    }

    @GetMapping("/program/recipients")
    public ResponseEntity<
            ApiResponse<
                    List<CampaignRecipientAnalyticsResponse>
                    >
            > getProgramRecipients() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Program campaign recipients retrieved successfully.",
                        analyticsService.getProgramRecipients()
                )
        );
    }

    @GetMapping("/{campaignId}/emails")
    public ResponseEntity<
            ApiResponse<List<CampaignEmailResponse>>
            > emails(
            @PathVariable UUID campaignId
    ) {

        campaignService.get(campaignId);

        List<CampaignEmailResponse> emails =
                campaignEmailRepository
                        .findAllByCampaignIdOrderByCreatedAtDesc(
                                campaignId
                        )
                        .stream()
                        .map(CampaignEmailResponse::from)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Campaign emails retrieved successfully.",
                        emails
                )
        );
    }

    @PostMapping("/recipients/{recipientId}/interested")
    public ResponseEntity<ApiResponse<Void>> markInterested(
            @PathVariable UUID recipientId
    ) {

        campaignRecipientRepository
                .findById(recipientId)
                .ifPresent(recipient -> {
                    recipient.markInterested();
                    campaignRecipientRepository.save(recipient);
                });

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Candidate marked as interested.",
                        null
                )
        );
    }
}