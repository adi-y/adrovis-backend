package com.adrovis.adrovis_backend.unpaidinternship.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "unpaid-internship.storage")
public class UnpaidInternshipStorageProperties {

    private String url;

    private String serviceKey;

    private String bucket;
}