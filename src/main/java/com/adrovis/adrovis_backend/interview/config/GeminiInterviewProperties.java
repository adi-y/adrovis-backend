package com.adrovis.adrovis_backend.interview.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.gemini")
public class GeminiInterviewProperties {

    private String apiKey;

    private String model = "gemini-2.5-flash";

    private String baseUrl =
            "https://generativelanguage.googleapis.com";

    private int maxResumeChars = 30000;

    /*
     * Gemini 2.5+ "thinking" models spend part of maxOutputTokens on
     * hidden reasoning tokens before writing visible output. If that
     * budget isn't reserved/limited, the visible JSON can be truncated
     * even when maxOutputTokens looks generous. 8192 leaves enough
     * headroom for 15 concise questions + a short closing pitch even
     * after thinking tokens are subtracted.
     */
    private int maxOutputTokens = 8192;

    /*
     * Caps tokens spent on internal "thinking" before the model writes
     * the actual JSON response. 0 disables extended thinking entirely,
     * which is what we want for a deterministic, schema-constrained
     * JSON generation task like this one.
     */
    private int thinkingBudget = 0;

    private double temperature = 0.2;

    private int maxRetries = 3;

    private long retryBaseDelayMs = 1000L;
}