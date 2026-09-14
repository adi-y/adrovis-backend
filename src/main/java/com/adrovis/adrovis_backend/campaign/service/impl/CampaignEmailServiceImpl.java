package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.config.CampaignProperties;
import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyType;
import com.adrovis.adrovis_backend.campaign.repository.CampaignEmailRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignEmailService;
import com.adrovis.adrovis_backend.campaign.service.CampaignLinkService;
import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.email.config.MailProperties;
import com.resend.Resend;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignEmailServiceImpl
        implements CampaignEmailService {

    private static final String OUTREACH_TEMPLATE =
            "email/campaign/campaign/CandidateOutreachFollowUpEmail.html";

    private static final String APPLICATION_TEMPLATE =
            "email/campaign/campaign/CampaignEmail.html";

    private final CampaignEmailRepository campaignEmailRepository;

    private final MailProperties mailProperties;

    private final CampaignLinkService campaignLinkService;

    private final CampaignProperties campaignProperties;

    private final ApplicationRepository applicationRepository;

    @Async("emailTaskExecutor")
    @Transactional
    @Override
    public void sendAsync(
            CampaignEmail campaignEmail
    ) {

        if (campaignEmail == null || campaignEmail.getId() == null) {
            log.warn(
                    "Campaign email send skipped because email/entity id is missing."
            );
            return;
        }

        try {

            CampaignEmail managed =
                    campaignEmailRepository
                            .findById(campaignEmail.getId())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Campaign email not found: "
                                                    + campaignEmail.getId()
                                    )
                            );

            /*
             * Only QUEUED emails are allowed to reach the provider.
             *
             * This prevents accidental duplicate provider sends when
             * an async invocation or manual retry reaches an already
             * processed email row.
             */
            if (managed.getStatus()
                    != com.adrovis.adrovis_backend.campaign.enums.CampaignEmailStatus.QUEUED) {

                log.debug(
                        "Campaign email send skipped because status is not QUEUED. " +
                                "emailId={}, status={}",
                        managed.getId(),
                        managed.getStatus()
                );

                return;
            }

            String html =
                    buildHtml(managed);

            Resend resend =
                    new Resend(
                            mailProperties.getApiKey()
                    );

            var request =
                    com.resend.services.emails.model.SendEmailRequest
                            .builder()
                            .from(
                                    "Adrovis <"
                                            + mailProperties.getFrom()
                                            + ">"
                            )
                            .to(
                                    managed.getToEmail()
                            )
                            .subject(
                                    managed.getSubject()
                            )
                            .html(
                                    html
                            )
                            .replyTo(
                                    campaignProperties.getReplyToEmail()
                            )
                            .build();

            var response =
                    resend.emails().send(request);

            managed.markSent(
                    response.getId()
            );

            campaignEmailRepository.save(
                    managed
            );

            log.info(
                    "Campaign email sent successfully. " +
                            "campaignId={}, emailId={}, candidateId={}, " +
                            "week={}, journeyType={}, journeyVersion={}, providerId={}",
                    managed.getCampaign().getId(),
                    managed.getId(),
                    managed.getCandidateId(),
                    managed.getWeekNumber(),
                    managed.getCampaignRecipient().getJourneyType(),
                    managed.getJourneyVersion(),
                    response.getId()
            );

        } catch (Exception ex) {

            try {

                CampaignEmail managed =
                        campaignEmailRepository
                                .findById(
                                        campaignEmail.getId()
                                )
                                .orElse(null);

                if (managed != null) {

                    managed.markFailed(
                            safeMessage(ex)
                    );

                    campaignEmailRepository.save(
                            managed
                    );
                }

            } catch (RuntimeException saveException) {

                log.error(
                        "Failed to persist campaign email failure. emailId={}",
                        campaignEmail.getId(),
                        saveException
                );
            }

            log.error(
                    "Campaign email failed. emailId={}, recipient={}",
                    campaignEmail.getId(),
                    campaignEmail.getToEmail(),
                    ex
            );
        }
    }

    /**
     * Selects the physical HTML template using the explicit journey type.
     *
     * IMPORTANT:
     *
     * Do NOT infer journey type from applicationId.
     *
     * A candidate's application state and campaign journey state are
     * separate concepts. CampaignRecipient.journeyType is authoritative.
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

    /**
     * Outreach follow-up emails intentionally use a completely separate
     * template.
     *
     * This template contains:
     *
     * - no fee placeholder
     * - no application reference
     * - no program snapshot
     * - no application-status block
     *
     * Therefore a fee cannot leak into an outreach email even if the
     * Campaign entity contains a fee snapshot.
     */
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

        /*
         * NOTE:
         *
         * There is intentionally no fee formatting here.
         * There is intentionally no campaign snapshot replacement here.
         *
         * This method cannot inject ₹499 because the outreach template
         * does not have fee/program-snapshot placeholders.
         */
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

    /**
     * Application follow-up emails continue to use the existing
     * application campaign template.
     *
     * This is the ONLY journey allowed to render:
     *
     * - fee
     * - application reference
     * - application-specific CTA
     * - program snapshot
     */
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

        /*
         * The campaign journey remains responsible for deciding WHEN an email
         * is sent. This method only decides the copy shown for the current
         * application journey week and current application state.
         *
         * PENDING:
         *   - asks the candidate to continue the existing application
         *
         * SUBMITTED:
         *   - asks the candidate to confirm continued interest
         *
         * The CTA itself is still resolved by resolveApplicationCtaLabel().
         * No campaign scheduling/business-state logic is changed here.
         */
        boolean submitted =
                email.getCampaignRecipient() != null
                        && "APPLICATION_SUBMITTED".equals(
                        String.valueOf(
                                email.getCampaignRecipient()
                                        .getEligibilityStatus()
                        )
                );

        String safeFee = escape(fee);

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

    /**
     * Outreach copy for the four weekly follow-ups.
     *
     * IMPORTANT:
     * No fee, registration charge, application reference, or application
     * status is included in outreach content.
     */
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

    private String safeMessage(
            Throwable throwable
    ) {

        if (throwable == null) {
            return "Unknown campaign email error.";
        }

        String message =
                throwable.getMessage();

        if (message == null
                || message.isBlank()) {

            return throwable
                    .getClass()
                    .getSimpleName();
        }

        return message.length() > 2000
                ? message.substring(0, 2000)
                : message;
    }

    private record OutreachContent(
            String headline,
            String body
    ) {
    }
}
