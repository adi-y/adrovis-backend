package com.adrovis.adrovis_backend.newsletter.repository;

import com.adrovis.adrovis_backend.newsletter.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsletterSubscriberRepository
        extends JpaRepository<NewsletterSubscriber, UUID> {

    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);
}