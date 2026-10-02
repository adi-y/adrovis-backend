package com.adrovis.adrovis_backend.unpaidinternship.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
        UnpaidInternshipStorageProperties.class
)
public class UnpaidInternshipStorageConfig {
}