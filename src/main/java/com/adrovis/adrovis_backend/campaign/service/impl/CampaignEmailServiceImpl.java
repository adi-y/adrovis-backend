package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.config.CampaignProperties;
import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyType;
import com.adrovis.adrovis_backend.campaign.repository.CampaignEmailRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignEmailService;
import com.adrovis.adrovis_backend.campaign.service.CampaignLinkService;
import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignEmailServiceImpl
        implements CampaignEmailService {

    private static final String BREVO_SEND_URL =
            "https://api.brevo.com/v3/smtp/email";

    private static final String OUTREACH_TEMPLATE =
            "email/campaign/campaign/CandidateOutreachFollowUpEmail.html";

    private static final String APPLICATION_TEMPLATE =
            "email/campaign/campaign/CampaignEmail.html";

    private final CampaignEmailRepository campaignEmailRepository;

    private final CampaignLinkService campaignLinkService;

    private final CampaignProperties campaignProperties;

    private final ApplicationRepository applicationRepository;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .build();

    /**
     * Sends exactly one campaign email through Brevo.
     *
     * IMPORTANT:
     *
     * - This method is called by CampaignDeliveryService.
     * - It is intentionally NOT @Async.
     * - Delivery is sequential so the application-side 300/day quota
     *   cannot be accidentally exceeded by concurrent sends.
     * - QUEUED emails are sent normally.
     * - Legacy FAILED emails are also allowed because they represent
     *   campaign emails that previously failed through the old Resend path.
     * - New Brevo failures are NOT marked FAILED.
     *   They remain QUEUED and will be picked up again later.
     */
    @Override
    @Transactional
    public void sendThroughBrevo(
            CampaignEmail campaignEmail
    ) {

        if (campaignEmail == null
                || campaignEmail.getId() == null) {

            log.warn(
                    "Campaign email send skipped because email/entity id is missing."
            );

            return;
        }

        CampaignEmail managed =
                campaignEmailRepository
                        .findById(
                                campaignEmail.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Campaign email not found: "
                                                + campaignEmail.getId()
                                )
                        );

        /*
         * SENT means this email has already been accepted by the provider.
         * Never send it again.
         */
        if (managed.getStatus()
                == CampaignEmailStatus.SENT) {

            log.debug(
                    "Campaign email already sent. emailId={}",
                    managed.getId()
            );

            return;
        }

        /*
         * Only QUEUED and legacy FAILED rows are eligible for the
         * Brevo recovery/delivery process.
         */
        if (managed.getStatus()
                != CampaignEmailStatus.QUEUED
                && managed.getStatus()
                != CampaignEmailStatus.FAILED) {

            log.debug(
                    "Campaign email send skipped because status is not deliverable. " +
                            "emailId={}, status={}",
                    managed.getId(),
                    managed.getStatus()
            );

            return;
        }

        try {

            String html =
                    buildHtml(managed);

            String messageId =
                    sendEmailToBrevo(
                            managed,
                            html
                    );

            managed.markSent(
                    messageId
            );

            campaignEmailRepository.save(
                    managed
            );

            log.info(
                    "Campaign email sent successfully through Brevo. " +
                            "campaignId={}, emailId={}, candidateId={}, " +
                            "week={}, journeyType={}, journeyVersion={}, brevoMessageId={}",
                    managed.getCampaign().getId(),
                    managed.getId(),
                    managed.getCandidateId(),
                    managed.getWeekNumber(),
                    managed.getCampaignRecipient().getJourneyType(),
                    managed.getJourneyVersion(),
                    messageId
            );

        } catch (Exception ex) {

            /*
             * IMPORTANT:
             *
             * Do NOT call markFailed().
             *
             * New Brevo/provider failures must remain retryable.
             *
             * If this row was an old FAILED row, it will remain FAILED.
             * The delivery scheduler intentionally includes FAILED rows,
             * so it will still be retried on the next delivery pass.
             *
             * If this row was QUEUED, it remains QUEUED.
             */
            log.error(
                    "Campaign email could not be sent through Brevo. " +
                            "It will remain retryable. emailId={}, recipient={}, status={}",
                    managed.getId(),
                    managed.getToEmail(),
                    managed.getStatus(),
                    ex
            );

            throw new CampaignEmailDeliveryException(
                    "Brevo campaign email delivery failed.",
                    ex
            );
        }
    }

    /**
     * Calls Brevo's transactional email API.
     *
     * POST https://api.brevo.com/v3/smtp/email
     */
    private String sendEmailToBrevo(
            CampaignEmail email,
            String html
    ) throws IOException, InterruptedException {

        String apiKey =
                campaignProperties.getBrevoApiKey();

        if (apiKey == null
                || apiKey.isBlank()) {

            throw new IllegalStateException(
                    "Brevo API key is not configured."
            );
        }

        String senderEmail =
                campaignProperties.getBrevoSenderEmail();

        if (senderEmail == null
                || senderEmail.isBlank()) {

            throw new IllegalStateException(
                    "Brevo sender email is not configured."
            );
        }

        String senderName =
                campaignProperties.getBrevoSenderName();

        String replyToEmail =
                campaignProperties.getReplyToEmail();

        String requestBody =
                buildBrevoRequestBody(
                        email,
                        html,
                        senderEmail,
                        senderName,
                        replyToEmail
                );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        BREVO_SEND_URL
                                )
                        )
                        .header(
                                "accept",
                                "application/json"
                        )
                        .header(
                                "api-key",
                                apiKey
                        )
                        .header(
                                "content-type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        requestBody,
                                        StandardCharsets.UTF_8
                                )
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString(
                                StandardCharsets.UTF_8
                        )
                );

        int statusCode =
                response.statusCode();

        String responseBody =
                response.body();

        if (statusCode < 200
                || statusCode >= 300) {

            throw new IllegalStateException(
                    "Brevo API returned HTTP "
                            + statusCode
                            + ": "
                            + safeResponse(
                            responseBody
                    )
            );
        }

        JsonNode responseJson =
                objectMapper.readTree(
                        responseBody
                );

        JsonNode messageIdNode =
                responseJson.get(
                        "messageId"
                );

        if (messageIdNode == null
                || messageIdNode.isNull()
                || messageIdNode.asText().isBlank()) {

            throw new IllegalStateException(
                    "Brevo API returned success but no messageId. " +
                            "Response: "
                            + safeResponse(responseBody)
            );
        }

        return messageIdNode.asText();
    }

    private String buildBrevoRequestBody(
            CampaignEmail email,
            String html,
            String senderEmail,
            String senderName,
            String replyToEmail
    ) throws IOException {

        var root =
                objectMapper.createObjectNode();

        var sender =
                root.putObject(
                        "sender"
                );

        sender.put(
                "email",
                senderEmail
        );

        if (senderName != null
                && !senderName.isBlank()) {

            sender.put(
                    "name",
                    senderName
            );
        }

        var toArray =
                root.putArray(
                        "to"
                );

        var recipient =
                toArray.addObject();

        recipient.put(
                "email",
                email.getToEmail()
        );

        root.put(
                "subject",
                email.getSubject()
        );

        root.put(
                "htmlContent",
                html
        );

        if (replyToEmail != null
                && !replyToEmail.isBlank()) {

            var replyTo =
                    root.putObject(
                            "replyTo"
                    );

            replyTo.put(
                    "email",
                    replyToEmail
            );
        }

        return objectMapper.writeValueAsString(
                root
        );
    }

    private String safeResponse(
            String response
    ) {

        if (response == null
                || response.isBlank()) {

            return "empty response";
        }

        return response.length() > 2000
                ? response.substring(
                0,
                2000
        )
                : response;
    }

    /**
     * Selects the physical HTML template using the explicit journey type.
     *
     * Do NOT infer journey type from applicationId.
     */
    private String buildHtml(
            CampaignEmail email
    ) throws IOException {

        if (email.getCampaignRecipient() == null) {

            throw new IllegalStateException(
                    "Campaign recipient is missing for campaign email: "
                            + email.getId()
            );
        }

        CampaignJourneyType journeyType =
                email.getCampaignRecipient()
                        .getJourneyType();

        if (journeyType == null) {

            throw new IllegalStateException(
                    "Campaign journey type is missing for campaign email: "
                            + email.getId()
            );
        }

        return switch (journeyType) {

            case OUTREACH_FOLLOW_UP ->
                    buildOutreachHtml(email);

            case APPLICATION_FOLLOW_UP ->
                    buildApplicationHtml(email);
        };
    }

    private String buildOutreachHtml(
            CampaignEmail email
    ) throws IOException {

        String html =
                loadTemplate(
                        OUTREACH_TEMPLATE
                );

        String clickUrl =
                campaignProperties.getPublicBaseUrl()
                        + "/careers/internship";

        String openToken =
                campaignLinkService.generateTrackingToken(
                        email.getId(),
                        "OPEN"
                );

        String openUrl =
                campaignProperties.getPublicBaseUrl()
                        + "/api/v1/public/campaigns/open?token="
                        + openToken;

        String candidateName =
                email.getCampaignRecipient()
                        .getCandidate()
                        .getName();

        OutreachContent content =
                outreachContent(
                        email.getWeekNumber()
                );

        String ctaLabel =
                "VIEW INTERNSHIP & APPLY";

        String ctaBlock =
                buildOutreachCtaBlock(
                        clickUrl,
                        ctaLabel
                );

        return html

                .replace(
                        "{{subject}}",
                        escape(
                                email.getSubject()
                        )
                )

                .replace(
                        "{{name}}",
                        escape(
                                candidateName
                        )
                )

                .replace(
                        "{{headline}}",
                        escape(
                                content.headline()
                        )
                )

                .replace(
                        "{{body}}",
                        content.body()
                )

                .replace(
                        "{{ctaBlock}}",
                        ctaBlock
                )

                .replace(
                        "{{unsubscribeUrl}}",
                        escapeAttribute(
                                email.getUnsubscribeUrl()
                        )
                )

                .replace(
                        "{{openUrl}}",
                        escapeAttribute(
                                openUrl
                        ));
    }

    private String buildApplicationHtml(
            CampaignEmail email
    ) throws IOException {

        String html =
                loadTemplate(
                        APPLICATION_TEMPLATE
                );

        String clickUrl =
                email.getCtaUrl();

        if (email.getCtaUrl() != null
                && email.getCtaUrl().startsWith("mailto:")
                && email.getCampaignRecipient() != null
                && email.getCampaignRecipient().getId() != null) {

            clickUrl =
                    campaignProperties.getPublicBaseUrl()
                            + "/api/v1/public/campaigns/interest?recipientId="
                            + email.getCampaignRecipient().getId();
        }

        String openToken =
                campaignLinkService.generateTrackingToken(
                        email.getId(),
                        "OPEN"
                );

        String openUrl =
                campaignProperties.getPublicBaseUrl()
                        + "/api/v1/public/campaigns/open?token="
                        + openToken;

        String candidateName =
                email.getCampaignRecipient()
                        .getCandidate()
                        .getName();

        String applicationReference =
                "";

        if (email.getApplicationId() != null) {

            applicationReference =
                    applicationRepository
                            .findById(
                                    email.getApplicationId()
                            )
                            .map(
                                    Application::getApplicationId
                            )
                            .orElse("");
        }

        String ctaBlock =
                "";

        if (email.getCtaUrl() != null
                && !email.getCtaUrl().isBlank()) {

            String ctaLabel =
                    resolveApplicationCtaLabel(
                            email
                    );

            ctaBlock =
                    buildApplicationCtaBlock(
                            clickUrl,
                            ctaLabel
                    );
        }

        String fee =
                formatFee(
                        email.getCampaign()
                                .getFeeAmountSnapshot(),
                        email.getCampaign()
                                .getCurrencySnapshot()
                );

        return html

                .replace(
                        "{{subject}}",
                        escape(
                                email.getSubject()
                        )
                )

                .replace(
                        "{{name}}",
                        escape(
                                candidateName
                        )
                )

                .replace(
                        "{{program}}",
                        escape(
                                email.getCampaign()
                                        .getProgramNameSnapshot()
                        )
                )

                .replace(
                        "{{body}}",
                        applicationEmailBody(
                                email,
                                fee
                        )
                )

                .replace(
                        "{{applicationId}}",
                        escape(
                                applicationReference
                        )
                )

                .replace(
                        "{{fee}}",
                        escape(
                                fee
                        )
                )

                .replace(
                        "{{duration}}",
                        escape(
                                email.getCampaign()
                                        .getDurationSnapshot()
                        )
                )

                .replace(
                        "{{mode}}",
                        escape(
                                email.getCampaign()
                                        .getModeSnapshot()
                        )
                )

                .replace(
                        "{{ctaBlock}}",
                        ctaBlock
                )

                .replace(
                        "{{unsubscribeUrl}}",
                        escapeAttribute(
                                email.getUnsubscribeUrl()
                        )
                )

                .replace(
                        "{{openUrl}}",
                        escapeAttribute(
                                openUrl
                        ));
    }

    private String loadTemplate(
            String classpath
    ) throws IOException {

        ClassPathResource resource =
                new ClassPathResource(
                        classpath
                );

        if (!resource.exists()) {

            throw new IOException(
                    "Campaign email template not found: "
                            + classpath
            );
        }

        return new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    private String buildOutreachCtaBlock(
            String clickUrl,
            String ctaLabel
    ) {

        return """
                <table role="presentation"
                       cellpadding="0"
                       cellspacing="0"
                       border="0">
                    <tr>
                        <td class="adr-cta-td"
                            align="center"
                            style="background-color:#0A0A0A;
                                   border-radius:4px;">

                            <!--[if mso]>
                            <v:roundrect
                                xmlns:v="urn:schemas-microsoft-com:vml"
                                href="{{clickUrl}}"
                                style="height:44px;
                                       v-text-anchor:middle;
                                       width:240px;"
                                arcsize="8%"
                                fillcolor="#0A0A0A"
                                stroke="f">
                                <w:anchorlock/>
                                <center style="
                                    color:#FFFFFF;
                                    font-family:Helvetica,Arial,sans-serif;
                                    font-size:12px;
                                    font-weight:bold;
                                    letter-spacing:1px;">
                                    {{ctaLabel}}
                                </center>
                            </v:roundrect>
                            <![endif]-->

                            <!--[if !mso]><!-->
                            <a href="{{clickUrl}}"
                               class="adr-cta-a"
                               target="_blank"
                               style="
                                   display:inline-block;
                                   padding:14px 32px;
                                   font-family:
                                       -apple-system,
                                       BlinkMacSystemFont,
                                       'Segoe UI',
                                       Roboto,
                                       Helvetica,
                                       Arial,
                                       sans-serif;
                                   font-size:12.5px;
                                   font-weight:700;
                                   letter-spacing:0.8px;
                                   color:#FFFFFF;
                                   text-transform:uppercase;
                                   border-radius:4px;
                                   background-color:#0A0A0A;">
                                {{ctaLabel}}
                            </a>
                            <!--<![endif]-->

                        </td>
                    </tr>
                </table>
                """
                .replace(
                        "{{clickUrl}}",
                        escapeAttribute(
                                clickUrl
                        )
                )
                .replace(
                        "{{ctaLabel}}",
                        escape(
                                ctaLabel
                        )
                );
    }

    private String buildApplicationCtaBlock(
            String clickUrl,
            String ctaLabel
    ) {

        return """
                <table role="presentation"
                       cellpadding="0"
                       cellspacing="0"
                       border="0">
                    <tr>
                        <td class="adr-cta-td"
                            align="center"
                            style="
                                background-color:#0A0A0A;
                                border-radius:5px;">

                            <!--[if mso]>
                            <v:roundrect
                                xmlns:v="urn:schemas-microsoft-com:vml"
                                href="{{clickUrl}}"
                                style="
                                    height:46px;
                                    v-text-anchor:middle;
                                    width:270px;"
                                arcsize="10%"
                                fillcolor="#0A0A0A"
                                stroke="f">

                                <w:anchorlock/>

                                <center style="
                                    color:#FFFFFF;
                                    font-family:Helvetica,Arial,sans-serif;
                                    font-size:12px;
                                    font-weight:bold;
                                    letter-spacing:0.7px;">
                                    {{ctaLabel}}
                                </center>

                            </v:roundrect>
                            <![endif]-->

                            <!--[if !mso]><!-->

                            <a href="{{clickUrl}}"
                               class="adr-cta-a"
                               target="_blank"
                               style="
                                   display:inline-block;
                                   padding:14px 28px;
                                   font-family:
                                       -apple-system,
                                       BlinkMacSystemFont,
                                       'Segoe UI',
                                       Roboto,
                                       Helvetica,
                                       Arial,
                                       sans-serif;
                                   font-size:12.5px;
                                   line-height:18px;
                                   font-weight:700;
                                   letter-spacing:0.7px;
                                   color:#FFFFFF;
                                   text-decoration:none;
                                   text-transform:uppercase;
                                   border-radius:5px;
                                   background-color:#0A0A0A;">

                                {{ctaLabel}}

                            </a>

                            <!--<![endif]-->

                        </td>
                    </tr>
                </table>
                """
                .replace(
                        "{{clickUrl}}",
                        escapeAttribute(
                                clickUrl
                        )
                )
                .replace(
                        "{{ctaLabel}}",
                        escape(
                                ctaLabel
                        )
                );
    }

    private String resolveApplicationCtaLabel(
            CampaignEmail email
    ) {

        if (email.getCtaUrl() == null
                || email.getCtaUrl().isBlank()) {

            return "";
        }

        if (email.getCtaUrl()
                .startsWith("mailto:")) {

            return "YES, I CONFIRM";
        }

        return "CONTINUE APPLICATION";
    }

    private String applicationEmailBody(
            CampaignEmail email,
            String fee
    ) {

        boolean submitted =
                email.getCampaignRecipient() != null
                        && "APPLICATION_SUBMITTED".equals(
                        String.valueOf(
                                email.getCampaignRecipient()
                                        .getEligibilityStatus()
                        )
                );

        if (submitted) {

            return switch (email.getEmailType()) {

                case WEEK_1_REENGAGEMENT -> """
                        <p style="margin:0 0 14px 0;">
                            Thank you for submitting your application for the
                            <strong style="color:#0A0A0A;">
                                Software Developer Internship
                            </strong>
                            at ADROVIS Technologies.
                        </p>

                        <p style="margin:0;">
                            We have your application with us and wanted to
                            confirm that you are still interested in proceeding
                            with the internship process.
                        </p>
                        """;

                case WEEK_2_EXPERIENCE -> """
                        <p style="margin:0 0 14px 0;">
                            Just following up on your
                            <strong style="color:#0A0A0A;">
                                Software Developer Internship
                            </strong>
                            application with ADROVIS Technologies.
                        </p>

                        <p style="margin:0;">
                            The internship includes practical exposure to
                            software development and client projects, along
                            with an understanding of how professional
                            engineering teams build and maintain systems at
                            scale.
                        </p>
                        """;

                case WEEK_3_ENGINEERING_WORKFLOW -> """
                        <p style="margin:0 0 14px 0;">
                            We wanted to check in regarding your
                            <strong style="color:#0A0A0A;">
                                Software Developer Internship
                            </strong>
                            application.
                        </p>

                        <p style="margin:0;">
                            If you are still interested, you can confirm below
                            and we will continue with the next steps.
                        </p>
                        """;

                case WEEK_4_OUTCOMES -> """
                        <p style="margin:0 0 14px 0;">
                            I wanted to follow up one final time regarding your
                            <strong style="color:#0A0A0A;">
                                Software Developer Internship
                            </strong>
                            application.
                        </p>

                        <p style="margin:0;">
                            If you would still like to proceed, please confirm
                            below. If you are no longer interested, no action
                            is needed.
                        </p>
                        """;
            };
        }

        return switch (email.getEmailType()) {

            case WEEK_1_REENGAGEMENT -> """
                    <p style="margin:0 0 14px 0;">
                        Thanks for starting your application for the
                        <strong style="color:#0A0A0A;">
                            Software Developer Internship
                        </strong>
                        at ADROVIS Technologies.
                    </p>

                    <p style="margin:0 0 14px 0;">
                        Your application is still pending, so you can continue
                        from where you left off using the link below.
                    </p>

                    <p style="margin:0;">
                        During the internship, you will get practical exposure
                        to software development and client projects, along with
                        an understanding of how professional engineering teams
                        build and maintain systems at scale.
                    </p>
                    """;

            case WEEK_2_EXPERIENCE -> """
                    <p style="margin:0 0 14px 0;">
                        Just following up on your
                        <strong style="color:#0A0A0A;">
                            Software Developer Internship
                        </strong>
                        application with ADROVIS Technologies.
                    </p>

                    <p style="margin:0;">
                        Your application is still pending. If you would like to
                        continue, you can pick it up from where you left off.
                    </p>
                    """;

            case WEEK_3_ENGINEERING_WORKFLOW -> """
                    <p style="margin:0 0 14px 0;">
                        I wanted to check in once more regarding your
                        <strong style="color:#0A0A0A;">
                            Software Developer Internship
                        </strong>
                        application.
                    </p>

                    <p style="margin:0;">
                        If you are still interested, you can continue your
                        application below. Your existing information will
                        remain available, so you do not need to start again.
                    </p>
                    """;

            case WEEK_4_OUTCOMES -> """
                    <p style="margin:0 0 14px 0;">
                        I wanted to follow up one final time regarding your
                        <strong style="color:#0A0A0A;">
                            Software Developer Internship
                        </strong>
                        application.
                    </p>

                    <p style="margin:0;">
                        If you would still like to continue with the process,
                        you can use the link below to complete your application.
                        If you are no longer interested, no action is needed.
                    </p>
                    """;
        };
    }

    private OutreachContent outreachContent(
            int week
    ) {

        return switch (week) {

            case 1 ->
                    new OutreachContent(
                            "",
                            """
                            <p style="margin:0 0 14px 0;">
                                Just following up on my earlier message regarding
                                the
                                <strong style="color:#0A0A0A;">
                                    Software Developer Internship
                                </strong>
                                at ADROVIS Technologies.
                            </p>

                            <p style="margin:0;">
                                The internship is focused on practical software
                                development, with exposure to development
                                workflows, Git, debugging, testing and code
                                reviews.
                            </p>
                            """
                    );

            case 2 ->
                    new OutreachContent(
                            "",
                            """
                            <p style="margin:0 0 14px 0;">
                                I wanted to follow up regarding the
                                <strong style="color:#0A0A0A;">
                                    Software Developer Internship
                                </strong>
                                at ADROVIS Technologies.
                            </p>

                            <p style="margin:0;">
                                The internship gives you practical exposure to
                                software development and the engineering
                                practices used to build, test and maintain
                                systems.
                            </p>
                            """
                    );

            case 3 ->
                    new OutreachContent(
                            "",
                            """
                            <p style="margin:0 0 14px 0;">
                                Just checking in regarding the
                                <strong style="color:#0A0A0A;">
                                    Software Developer Internship
                                </strong>
                                at ADROVIS Technologies.
                            </p>

                            <p style="margin:0;">
                                The internship provides exposure to practical
                                development work, including Git, debugging,
                                testing, code reviews and working with client
                                projects.
                            </p>
                            """
                    );

            case 4 ->
                    new OutreachContent(
                            "",
                            """
                            <p style="margin:0 0 14px 0;">
                                I wanted to send one final follow-up regarding
                                the
                                <strong style="color:#0A0A0A;">
                                    Software Developer Internship
                                </strong>
                                at ADROVIS Technologies.
                            </p>

                            <p style="margin:0;">
                                If you are looking for practical software
                                development experience and would like to explore
                                the opportunity, you can find the complete
                                details below.
                            </p>
                            """
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported outreach campaign week: "
                                    + week
                    );
        };
    }

    private String formatFee(
            BigDecimal amount,
            String currency
    ) {

        if (amount == null) {
            return "";
        }

        String formattedAmount =
                amount
                        .stripTrailingZeros()
                        .toPlainString();

        String normalizedCurrency =
                currency == null
                        ? ""
                        : currency
                        .trim()
                        .toUpperCase();

        if ("INR".equals(normalizedCurrency)) {

            return "₹" + formattedAmount;
        }

        if (normalizedCurrency.isBlank()) {

            return formattedAmount;
        }

        return formattedAmount
                + " "
                + normalizedCurrency;
    }

    private String escape(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeAttribute(
            String value
    ) {

        return escape(value);
    }

    private record OutreachContent(
            String headline,
            String body
    ) {
    }

    /**
     * Runtime exception used specifically for Brevo campaign delivery.
     *
     * The delivery scheduler catches this exception and moves on to the
     * next queued email. The database row remains retryable.
     */
    private static class CampaignEmailDeliveryException
            extends RuntimeException {

        public CampaignEmailDeliveryException(
                String message,
                Throwable cause
        ) {
            super(
                    message,
                    cause
            );
        }
    }
}