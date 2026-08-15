package com.adrovis.adrovis_backend.newsletter.controller;

import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import com.adrovis.adrovis_backend.newsletter.dto.NewsletterSubscribeRequest;
import com.adrovis.adrovis_backend.newsletter.dto.NewsletterSubscribeResponse;
import com.adrovis.adrovis_backend.newsletter.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(
        name = "Newsletter",
        description = "Newsletter subscription and administrative subscriber APIs."
)
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/newsletter/subscribe")
    @Operation(
            summary = "Subscribe to newsletter",
            description = "Subscribes an email address to the Adrovis newsletter."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Successfully subscribed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid email or request"
            )
    })
    public ResponseEntity<ApiResponse<NewsletterSubscribeResponse>> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request
    ) {

        NewsletterSubscribeResponse response =
                newsletterService.subscribe(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED,
                                "Successfully subscribed to the newsletter.",
                                response
                        )
                );
    }

    @GetMapping("/admin/newsletter/subscribers")
    @Operation(
            summary = "Get newsletter subscribers",
            description = """
                    Retrieves all newsletter subscribers.
                    Intended for administrative use.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscribers retrieved successfully"
            )
    })
    public ResponseEntity<
            ApiResponse<List<NewsletterSubscribeResponse>>
            > getAllSubscribers() {

        List<NewsletterSubscribeResponse> subscribers =
                newsletterService.getAllSubscribers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Newsletter subscribers retrieved successfully.",
                        subscribers
                )
        );
    }

    @GetMapping("/admin/newsletter/subscribers/{subscriberId}")
    @Operation(
            summary = "Get newsletter subscriber by ID",
            description = """
                    Retrieves a single newsletter subscriber by UUID.
                    Intended for administrative use.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscriber retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscriber not found"
            )
    })
    public ResponseEntity<
            ApiResponse<NewsletterSubscribeResponse>
            > getSubscriberById(
            @PathVariable UUID subscriberId
    ) {

        NewsletterSubscribeResponse response =
                newsletterService.getSubscriberById(subscriberId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Newsletter subscriber retrieved successfully.",
                        response
                )
        );
    }
}