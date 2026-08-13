package com.adrovis.adrovis_backend.newsletter.dto.response;

import com.adrovis.adrovis_backend.newsletter.enums.NewsletterSubscriberStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscribeResponse {

    private String email;

    private NewsletterSubscriberStatus status;
}