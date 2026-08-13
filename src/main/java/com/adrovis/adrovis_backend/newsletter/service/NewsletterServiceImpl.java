package com.adrovis.adrovis_backend.newsletter.service;

import com.adrovis.adrovis_backend.newsletter.dto.request.NewsletterSubscribeRequest;
import com.adrovis.adrovis_backend.newsletter.dto.response.NewsletterSubscribeResponse;
import com.adrovis.adrovis_backend.newsletter.entity.NewsletterSubscriber;
import com.adrovis.adrovis_backend.newsletter.enums.NewsletterSubscriberStatus;
import com.adrovis.adrovis_backend.newsletter.repository.NewsletterSubscriberRepository;
import com.adrovis.adrovis_backend.newsletter.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

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
}