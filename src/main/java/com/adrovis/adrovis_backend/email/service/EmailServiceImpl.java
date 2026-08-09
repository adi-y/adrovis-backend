package com.adrovis.adrovis_backend.email.service;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.email.config.MailProperties;
import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final MailProperties mailProperties;

    @Value("${app.candidate-portal-base-url:http://localhost:3000}")
    private String candidatePortalBaseUrl;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

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

    @Async("emailTaskExecutor")
    @Override
    public void sendApplicationShortlistedEmailAsync(Application application) {

        try {

            String html = buildApplicationShortlistedTemplate(application);

            Resend resend = new Resend(mailProperties.getApiKey());

            SendEmailRequest request = SendEmailRequest.builder()
                    .from("Adrovis <" + mailProperties.getFrom() + ">")
                    .to(application.getApplicantEmail())
                    .subject("You've Been Shortlisted - Adrovis")
                    .html(html)
                    .build();

            var response = resend.emails().send(request);

            log.info(
                    "Application shortlisted email sent successfully. " +
                            "recipient={}, applicationId={}, resendId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    response.getId()
            );

        } catch (ResendException | IOException ex) {

            log.error(
                    "Failed to send application shortlisted email. " +
                            "recipient={}, applicationId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    ex
            );
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendApplicationRejectedEmailAsync(Application application) {

        try {

            String html = buildApplicationRejectedTemplate(application);

            Resend resend = new Resend(mailProperties.getApiKey());

            SendEmailRequest request = SendEmailRequest.builder()
                    .from("Adrovis <" + mailProperties.getFrom() + ">")
                    .to(application.getApplicantEmail())
                    .subject("Application Update - Adrovis")
                    .html(html)
                    .build();

            var response = resend.emails().send(request);

            log.info(
                    "Application rejected email sent successfully. " +
                            "recipient={}, applicationId={}, resendId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    response.getId()
            );

        } catch (ResendException | IOException ex) {

            log.error(
                    "Failed to send application rejected email. " +
                            "recipient={}, applicationId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    ex
            );
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendInterviewAvailabilityRequestEmailAsync(Application application) {

        try {

            String html = buildInterviewAvailabilityRequestTemplate(application);

            Resend resend = new Resend(mailProperties.getApiKey());

            SendEmailRequest request = SendEmailRequest.builder()
                    .from("Adrovis <" + mailProperties.getFrom() + ">")
                    .to(application.getApplicantEmail())
                    .subject("Interview Availability Requested - Adrovis")
                    .html(html)
                    .build();

            var response = resend.emails().send(request);

            log.info(
                    "Interview availability request email sent successfully. " +
                            "recipient={}, applicationId={}, resendId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    response.getId()
            );

        } catch (ResendException | IOException ex) {

            log.error(
                    "Failed to send interview availability request email. " +
                            "recipient={}, applicationId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    ex
            );
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendInterviewScheduledEmailAsync(Application application, Interview interview) {

        try {

            String html = buildInterviewScheduledTemplate(application, interview);

            Resend resend = new Resend(mailProperties.getApiKey());

            SendEmailRequest request = SendEmailRequest.builder()
                    .from("Adrovis <" + mailProperties.getFrom() + ">")
                    .to(application.getApplicantEmail())
                    .subject("Your Interview Is Confirmed - Adrovis")
                    .html(html)
                    .build();

            var response = resend.emails().send(request);

            log.info(
                    "Interview scheduled email sent successfully. " +
                            "recipient={}, applicationId={}, resendId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    response.getId()
            );

        } catch (ResendException | IOException ex) {

            log.error(
                    "Failed to send interview scheduled email. " +
                            "recipient={}, applicationId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    ex
            );
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendInterviewRescheduledEmailAsync(Application application, Interview interview) {

        try {

            String html = buildInterviewRescheduledTemplate(application, interview);

            Resend resend = new Resend(mailProperties.getApiKey());

            SendEmailRequest request = SendEmailRequest.builder()
                    .from("Adrovis <" + mailProperties.getFrom() + ">")
                    .to(application.getApplicantEmail())
                    .subject("Your Interview Time Has Changed - Adrovis")
                    .html(html)
                    .build();

            var response = resend.emails().send(request);

            log.info(
                    "Interview rescheduled email sent successfully. " +
                            "recipient={}, applicationId={}, resendId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    response.getId()
            );

        } catch (ResendException | IOException ex) {

            log.error(
                    "Failed to send interview rescheduled email. " +
                            "recipient={}, applicationId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    ex
            );
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendInterviewCancelledEmailAsync(Application application, Interview interview) {

        try {

            String html = buildInterviewCancelledTemplate(application, interview);

            Resend resend = new Resend(mailProperties.getApiKey());

            SendEmailRequest request = SendEmailRequest.builder()
                    .from("Adrovis <" + mailProperties.getFrom() + ">")
                    .to(application.getApplicantEmail())
                    .subject("Your Interview Has Been Cancelled - Adrovis")
                    .html(html)
                    .build();

            var response = resend.emails().send(request);

            log.info(
                    "Interview cancelled email sent successfully. " +
                            "recipient={}, applicationId={}, resendId={}",
                    application.getApplicantEmail(),
                    application.getApplicationId(),
                    response.getId()
            );

        } catch (ResendException | IOException ex) {

            log.error(
                    "Failed to send interview cancelled email. " +
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

    private String buildApplicationShortlistedTemplate(Application application)
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource("email/ApplicationShortlistedEmail.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        String availabilityLink = candidatePortalBaseUrl
                + "/applications/" + application.getApplicationId() + "/availability";

        return html
                .replace("{{name}}", application.getApplicantName())
                .replace("{{applicationId}}", application.getApplicationId())
                .replace("{{program}}", application.getJobTitleSnapshot())
                .replace("{{availabilityLink}}", availabilityLink);
    }

    private String buildApplicationRejectedTemplate(Application application)
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource("email/ApplicationRejectedEmail.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return html
                .replace("{{name}}", application.getApplicantName())
                .replace("{{applicationId}}", application.getApplicationId())
                .replace("{{program}}", application.getJobTitleSnapshot());
    }

private String buildInterviewAvailabilityRequestTemplate(Application application)
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource("email/ApplicationShortlistedEmail.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        String availabilityLink = candidatePortalBaseUrl
                + "/applications/" + application.getApplicationId() + "/availability";

        return html
                .replace("{{name}}", application.getApplicantName())
                .replace("{{applicationId}}", application.getApplicationId())
                .replace("{{program}}", application.getJobTitleSnapshot())
                .replace("{{availabilityLink}}", availabilityLink);
    }

    private String buildInterviewScheduledTemplate(Application application, Interview interview)
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource("email/InterviewScheduledEmail.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return html
                .replace("{{name}}", application.getApplicantName())
                .replace("{{applicationId}}", application.getApplicationId())
                .replace("{{program}}", application.getJobTitleSnapshot())
                .replace("{{scheduledDate}}", formatDate(interview))
                .replace("{{scheduledTimeRange}}", formatTimeRange(interview))
                .replace("{{timezone}}", interview.getDisplayTimezone())
                .replace("{{interviewerName}}", nullToEmpty(interview.getInterviewerName()))
                .replace("{{meetingLink}}", nullToEmpty(interview.getMeetingLink()));
    }

    private String buildInterviewRescheduledTemplate(Application application, Interview interview)
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource("email/InterviewRescheduledEmail.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return html
                .replace("{{name}}", application.getApplicantName())
                .replace("{{applicationId}}", application.getApplicationId())
                .replace("{{program}}", application.getJobTitleSnapshot())
                .replace("{{scheduledDate}}", formatDate(interview))
                .replace("{{scheduledTimeRange}}", formatTimeRange(interview))
                .replace("{{timezone}}", interview.getDisplayTimezone())
                .replace("{{interviewerName}}", nullToEmpty(interview.getInterviewerName()))
                .replace("{{meetingLink}}", nullToEmpty(interview.getMeetingLink()));
    }

    private String buildInterviewCancelledTemplate(Application application, Interview interview)
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource("email/InterviewCancelledEmail.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        String reasonBlock = "";
        if (interview.getAdminNote() != null && !interview.getAdminNote().isBlank()) {
            reasonBlock = "<p style=\"margin:0 0 16px 0;font-size:15px;color:#333333;line-height:1.6;\">"
                    + "Note from our team: " + interview.getAdminNote() + "</p>";
        }

        return html
                .replace("{{name}}", application.getApplicantName())
                .replace("{{applicationId}}", application.getApplicationId())
                .replace("{{program}}", application.getJobTitleSnapshot())
                .replace("{{reasonBlock}}", reasonBlock);
    }

    private String formatDate(Interview interview) {
        ZonedDateTime zoned = interview.getScheduledStartUtc()
                .atZoneSameInstant(ZoneId.of(interview.getDisplayTimezone()));
        return zoned.format(DATE_FORMATTER);
    }

    private String formatTimeRange(Interview interview) {
        ZoneId zone = ZoneId.of(interview.getDisplayTimezone());
        ZonedDateTime start = interview.getScheduledStartUtc().atZoneSameInstant(zone);
        ZonedDateTime end = interview.getScheduledEndUtc().atZoneSameInstant(zone);
        return start.format(TIME_FORMATTER) + " - " + end.format(TIME_FORMATTER);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
