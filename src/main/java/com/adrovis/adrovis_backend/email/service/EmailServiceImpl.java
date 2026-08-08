package com.adrovis.adrovis_backend.email.service;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.email.config.MailProperties;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final MailProperties mailProperties;

    @Async("emailTaskExecutor")
    @Override
    public void sendApplicationReceivedEmailAsync(Application application) {

        try {

            String html = buildApplicationReceivedTemplate(application);

            Resend resend = new Resend(mailProperties.getApiKey());

            SendEmailRequest request = SendEmailRequest.builder()
                    .from("Adrovis <" + mailProperties.getFrom() + ">")
                    .to(application.getApplicantEmail())
                    .subject("Application Received - Adrovis")
                    .html(html)
                    .build();

            var response = resend.emails().send(request);

            log.info(
                    "Application confirmation email sent successfully. " +
                            "recipient={}, applicationId={}, resendId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    response.getId()
            );

        } catch (ResendException | IOException ex) {

            log.error(
                    "Failed to send application confirmation email. " +
                            "recipient={}, applicationId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    ex
            );
        }
    }

    private String buildApplicationReceivedTemplate(Application application)
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource("email/ApplicationReceivedEmail.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return html
                .replace("{{name}}", application.getApplicantName())
                .replace("{{applicationId}}", application.getApplicationId())
                .replace("{{program}}", application.getJobTitleSnapshot());
    }
}