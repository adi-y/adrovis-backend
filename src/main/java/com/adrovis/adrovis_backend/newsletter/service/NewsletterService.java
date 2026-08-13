package com.adrovis.adrovis_backend.newsletter.service;

import com.adrovis.adrovis_backend.newsletter.dto.request.NewsletterSubscribeRequest;
import com.adrovis.adrovis_backend.newsletter.dto.response.NewsletterSubscribeResponse;

public interface NewsletterService {

    NewsletterSubscribeResponse subscribe(
            NewsletterSubscribeRequest request
    );
}