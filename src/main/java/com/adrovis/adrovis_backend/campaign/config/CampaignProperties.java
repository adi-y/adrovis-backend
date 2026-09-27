package com.adrovis.adrovis_backend.campaign.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.campaign")
public class CampaignProperties {

    /**
     * Base URL of the public ADROVIS website.
     *
     * Kept for compatibility with existing campaign configuration.
     */
    private String baseUrl = "https://www.adrovis.com";

    private String frontendBaseUrl;

    /**
     * Public backend/application base URL used for:
     * - unsubscribe links
     * - open tracking
     * - click tracking
     */
    private String publicBaseUrl = "https://www.adrovis.com";

    /**
     * Email address used for:
     * - campaign Reply-To
     * - candidate YES confirmation mailto CTA
     */
    private String replyToEmail = "hello@adrovis.com";

    /**
     * Secret used to sign:
     * - continuation tokens
     * - unsubscribe tokens
     * - tracking tokens
     */
    private String continuationSecret;

    /**
     * Validity period for campaign-generated tokens.
     *
     * 720 hours = 30 days.
     */
    private long continuationValidityHours = 720;

    /**
     * Only this program is automated by the campaign system.
     *
     * JOB applications and job outreach are intentionally excluded.
     */
    private String programKey = "SOFTWARE_DEVELOPER_INTERNSHIP";

    /**
     * Campaign scheduler polling interval.
     *
     * Production default:
     * 1 hour.
     */
    private long schedulerDelayMs = 3_600_000L;

    /**
     * Campaign safety switch.
     */
    private boolean testMode = false;

    /**
     * The ONLY email address allowed to receive automated campaign
     * follow-ups while testMode=true.
     */
    private String testEmail;

    /**
     * Duration of one campaign step.
     *
     * Production:
     * 7 days.
     *
     * Test:
     * accelerated duration.
     */
    private long weekDurationSeconds = 604_800L;

    /**
     * Production daily campaign send time in local campaign timezone.
     */
    private String dailySendTime = "10:00";

    /**
     * Timezone used for campaign scheduling and daily quota calculation.
     */
    private String timezone = "Asia/Kolkata";

    /**
     * Maximum number of candidate journeys processed in one scheduler pass.
     */
    private int batchSize = 500;

    /**
     * ================================================================
     * BREVO CAMPAIGN DELIVERY CONFIGURATION
     * ================================================================
     */

    /**
     * Brevo API key used ONLY for campaign emails.
     */
    private String brevoApiKey;

    /**
     * Verified Brevo sender email.
     *
     * Example:
     * hello@adrovis.com
     */
    private String brevoSenderEmail;

    /**
     * Sender display name.
     */
    private String brevoSenderName = "Adrovis";

    /**
     * Application-side daily campaign quota.
     *
     * Brevo Free plan currently provides 300 email sends/day.
     */
    private int brevoDailyQuota = 300;

    /**
     * Maximum number of campaign emails processed in one delivery pass.
     *
     * This should normally remain 300.
     */
    private int brevoDeliveryBatchSize = 300;

    /**
     * Delivery scheduler interval.
     *
     * Production default:
     * 1 hour.
     */
    private long brevoDeliverySchedulerDelayMs = 3_600_000L;
}