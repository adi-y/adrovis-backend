package com.adrovis.adrovis_backend.newsletter.service;

import com.adrovis.adrovis_backend.newsletter.dto.NewsletterSubscribeRequest;
import com.adrovis.adrovis_backend.newsletter.dto.NewsletterSubscribeResponse;

import java.util.List;
import java.util.UUID;

public interface NewsletterService {

    NewsletterSubscribeResponse subscribe(
            NewsletterSubscribeRequest request
    );

    List<NewsletterSubscribeResponse> getAllSubscribers();

    NewsletterSubscribeResponse getSubscriberById(
            UUID subscriberId
    );
}