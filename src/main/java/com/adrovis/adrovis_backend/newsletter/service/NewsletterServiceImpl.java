package com.adrovis.adrovis_backend.newsletter.service;

import com.adrovis.adrovis_backend.common.exception.ResourceNotFoundException;
import com.adrovis.adrovis_backend.newsletter.dto.NewsletterSubscribeRequest;
import com.adrovis.adrovis_backend.newsletter.dto.NewsletterSubscribeResponse;
import com.adrovis.adrovis_backend.newsletter.entity.NewsletterSubscriber;
import com.adrovis.adrovis_backend.newsletter.enums.NewsletterSubscriberStatus;
import com.adrovis.adrovis_backend.newsletter.repository.NewsletterSubscriberRepository;
import com.adrovis.adrovis_backend.newsletter.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {

    private final NewsletterSubscriberRepository newsletterSubscriberRepository;

    @Override
    @Transactional
    public NewsletterSubscribeResponse subscribe(
            NewsletterSubscribeRequest request
    ) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        NewsletterSubscriber subscriber =
                newsletterSubscriberRepository
                        .findByEmailIgnoreCase(email)
                        .orElse(null);

        if (subscriber == null) {

            subscriber = NewsletterSubscriber.builder()
                    .email(email)
                    .status(NewsletterSubscriberStatus.SUBSCRIBED)
                    .subscribedAt(LocalDateTime.now())
                    .build();

        } else if (
                subscriber.getStatus()
                        == NewsletterSubscriberStatus.UNSUBSCRIBED
        ) {

            subscriber.setStatus(
                    NewsletterSubscriberStatus.SUBSCRIBED
            );

            subscriber.setSubscribedAt(LocalDateTime.now());
            subscriber.setUnsubscribedAt(null);
        }

        subscriber =
                newsletterSubscriberRepository.save(subscriber);

        return NewsletterSubscribeResponse.builder()
                .email(subscriber.getEmail())
                .status(subscriber.getStatus())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public List<NewsletterSubscribeResponse> getAllSubscribers() {

        return newsletterSubscriberRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NewsletterSubscribeResponse getSubscriberById(
            UUID subscriberId
    ) {

        NewsletterSubscriber subscriber =
                newsletterSubscriberRepository
                        .findById(subscriberId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Newsletter subscriber not found."
                                )
                        );

        return toResponse(subscriber);
    }

    private NewsletterSubscribeResponse toResponse(
            NewsletterSubscriber subscriber
    ) {

        return NewsletterSubscribeResponse.builder()
                .id(subscriber.getId())
                .email(subscriber.getEmail())
                .status(subscriber.getStatus())
                .subscribedAt(subscriber.getSubscribedAt())
                .unsubscribedAt(subscriber.getUnsubscribedAt())
                .createdAt(subscriber.getCreatedAt())
                .updatedAt(subscriber.getUpdatedAt())
                .build();
    }
}