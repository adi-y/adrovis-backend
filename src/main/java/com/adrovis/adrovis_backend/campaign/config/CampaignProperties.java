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
     * Scheduler polling interval.
     *
     * Production default:
     * 1 hour.
     *
     * Test configuration:
     * usually overridden to 2 minutes.
     */
    private long schedulerDelayMs = 3_600_000L;

    /**
     * Campaign safety switch.
     *
     * false:
     * normal production audience is eligible for processing.
     *
     * true:
     * ONLY testEmail is allowed to be processed by the automated
     * campaign scheduler, and accelerated journey timing is used.
     *
     * IMPORTANT:
     * This flag controls the campaign scheduler.
     * It does not restrict the initial admin candidate-outreach API.
     */
    private boolean testMode = false;

    /**
     * The ONLY email address allowed to receive automated campaign
     * follow-ups while testMode=true.
     *
     * This should be explicitly configured in the environment.
     */
    private String testEmail;

    /**
     * Duration of one campaign step.
     *
     * Production:
     * Java campaign logic schedules steps using real calendar dates,
     * so this value is not used as a one-week approximation.
     *
     * Test:
     * This is the accelerated duration between campaign weeks.
     *
     * Example:
     * 120 seconds = 2 minutes per campaign step.
     */
    private long weekDurationSeconds = 604_800L;

    /**
     * Production daily campaign send time in local campaign timezone.
     *
     * Default:
     * 10:00 AM Asia/Kolkata.
     */
    private String dailySendTime = "10:00";

    /**
     * Timezone used for production campaign scheduling.
     */
    private String timezone = "Asia/Kolkata";

    /**
     * Maximum number of candidate journeys processed in one scheduler pass.
     *
     * Prevents the scheduler from loading the complete campaign audience
     * into memory at once.
     */
    private int batchSize = 500;
}