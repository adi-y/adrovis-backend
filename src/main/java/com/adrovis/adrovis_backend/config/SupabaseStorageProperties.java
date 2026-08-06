package com.adrovis.adrovis_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Supabase Storage.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "supabase")
public class SupabaseStorageProperties {

    /**
     * Example:
     * https://xxxxxxxx.supabase.co
     */
    private String url;

    /**
     * Service Role Key.
     *
     * Never expose this outside the backend.
     */
    private String serviceKey;

    /**
     * Bucket name.
     */
    private String bucket;

}