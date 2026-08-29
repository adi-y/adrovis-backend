package com.adrovis.adrovis_backend.interview.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.gemini-copilot")
public class InterviewCopilotProperties {

    private String apiKey;

    private String model = "gemini-3.6-flash";

    private String baseUrl =
            "https://generativelanguage.googleapis.com";

    private int maxOutputTokens = 8000;

    private double temperature = 0.2;

    private int maxInputChars = 24000;

    private int maxNotes = 50;

    private int maxRetries = 1;

    private long retryBaseDelayMs = 600L;
}