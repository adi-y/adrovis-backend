package com.adrovis.adrovis_backend.newsletter.dto;

import com.adrovis.adrovis_backend.newsletter.enums.NewsletterSubscriberStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscribeResponse {

    private UUID id;

    private String email;

    private NewsletterSubscriberStatus status;

    private LocalDateTime subscribedAt;

    private LocalDateTime unsubscribedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}