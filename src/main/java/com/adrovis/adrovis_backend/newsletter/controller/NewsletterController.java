package com.adrovis.adrovis_backend.newsletter.controller;

import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import com.adrovis.adrovis_backend.newsletter.dto.request.NewsletterSubscribeRequest;
import com.adrovis.adrovis_backend.newsletter.dto.response.NewsletterSubscribeResponse;
import com.adrovis.adrovis_backend.newsletter.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
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
}