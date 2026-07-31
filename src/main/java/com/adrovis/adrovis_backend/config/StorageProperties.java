package com.adrovis.adrovis_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * Root directory where uploaded files are stored.
     */
    private String uploadDir;

    /**
     * Maximum allowed upload size.
     */
    private DataSize maxFileSize;

    /**
     * Allowed MIME types.
     */
    private List<String> allowedTypes;
}