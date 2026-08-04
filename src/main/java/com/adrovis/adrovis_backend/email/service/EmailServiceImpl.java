package com.adrovis.adrovis_backend.email.service;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.email.config.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    @Async("emailTaskExecutor")
    @Override
    public void sendApplicationReceivedEmailAsync(Application application) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());


            helper.setFrom(mailProperties.getFrom());
            helper.setTo(application.getApplicantEmail());
            helper.setSubject("Application Received - Adrovis");

            helper.setText(buildApplicationReceivedTemplate(application), true);

            mailSender.send(message);

            log.info(
                    "Application confirmation email sent successfully to {}",
                    application.getApplicantEmail()
            );

        } catch (MailException | MessagingException | IOException ex) {

            log.error(
                    "Failed to send application confirmation email to {}",
                    application.getApplicantEmail(),
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