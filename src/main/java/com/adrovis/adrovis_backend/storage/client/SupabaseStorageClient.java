package com.adrovis.adrovis_backend.storage.client;

import com.adrovis.adrovis_backend.common.exception.FileStorageException;
import com.adrovis.adrovis_backend.config.SupabaseStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupabaseStorageClient {

    private final RestClient restClient;
    private final SupabaseStorageProperties properties;

    public void upload(
            String storagePath,
            byte[] bytes,
            String contentType
    ) {

        try {

            String uploadUrl =
                    properties.getUrl()
                            + "/storage/v1/object/"
                            + properties.getBucket()
                            + "/"
                            + storagePath;

            log.debug("Supabase upload URL: {}", uploadUrl);

            restClient.post()

                    .uri(URI.create(uploadUrl))

                    .header("apikey", properties.getServiceKey())
                    .header(
                            "Authorization",
                            "Bearer " + properties.getServiceKey()
                    )
                    .header("x-upsert", "false")

                    .contentType(MediaType.parseMediaType(contentType))

                    .body(new ByteArrayResource(bytes))

                    .retrieve()

                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {

                                throw new FileStorageException(
                                        "Supabase upload failed. HTTP "
                                                + response.getStatusCode().value()
                                );
                            })

                    .toBodilessEntity();

            log.info(
                    "Uploaded file to Supabase: {}",
                    storagePath
            );

        } catch (RestClientResponseException ex) {

            log.error(
                    "Supabase upload failed: {}",
                    ex.getResponseBodyAsString(),
                    ex
            );

            throw new FileStorageException(
                    "Failed to upload file to Supabase Storage.",
                    ex
            );

        } catch (IllegalArgumentException ex) {

            log.error(
                    "Invalid Supabase upload URL. storagePath={}",
                    storagePath,
                    ex
            );

            throw new FileStorageException(
                    "Invalid Supabase Storage URL.",
                    ex
            );
        }
    }

    public void delete(String storagePath) {

        try {

            String deleteUrl =
                    properties.getUrl()
                            + "/storage/v1/object/"
                            + properties.getBucket()
                            + "/"
                            + storagePath;

            log.debug("Supabase delete URL: {}", deleteUrl);

            restClient.delete()

                    .uri(URI.create(deleteUrl))

                    .header("apikey", properties.getServiceKey())

                    .header(
                            "Authorization",
                            "Bearer " + properties.getServiceKey()
                    )

                    .retrieve()

                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {

                                throw new FileStorageException(
                                        "Supabase delete failed. HTTP "
                                                + response.getStatusCode().value()
                                );
                            })

                    .toBodilessEntity();

            log.info(
                    "Deleted file from Supabase: {}",
                    storagePath
            );

        } catch (RestClientResponseException ex) {

            log.error(
                    "Supabase delete failed: {}",
                    ex.getResponseBodyAsString(),
                    ex
            );

            throw new FileStorageException(
                    "Failed to delete file from Supabase Storage.",
                    ex
            );

        } catch (IllegalArgumentException ex) {

            log.error(
                    "Invalid Supabase delete URL. storagePath={}",
                    storagePath,
                    ex
            );

            throw new FileStorageException(
                    "Invalid Supabase Storage URL.",
                    ex
            );
        }
    }

    public String getPublicUrl(String storagePath) {

        return properties.getUrl()
                + "/storage/v1/object/public/"
                + properties.getBucket()
                + "/"
                + storagePath;
    }

    public byte[] download(String storagePath) {

        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            throw new FileStorageException(
                    "Supabase Storage URL is not configured (supabase.url is null/blank)."
            );
        }

        if (properties.getBucket() == null || properties.getBucket().isBlank()) {
            throw new FileStorageException(
                    "Supabase Storage bucket is not configured (supabase.bucket is null/blank)."
            );
        }

        if (properties.getServiceKey() == null || properties.getServiceKey().isBlank()) {
            throw new FileStorageException(
                    "Supabase Storage service key is not configured (supabase.service-key is null/blank)."
            );
        }

        String downloadUrl =
                properties.getUrl()
                        + "/storage/v1/object/"
                        + properties.getBucket()
                        + "/"
                        + storagePath;

        log.info(
                "Downloading resume from Supabase. bucket={}, storagePath={}, url={}",
                properties.getBucket(),
                storagePath,
                downloadUrl
        );

        try {

            URI downloadUri = URI.create(downloadUrl);

            return restClient.get()

                    .uri(downloadUri)

                    .header("apikey", properties.getServiceKey())
                    .header("Authorization", "Bearer " + properties.getServiceKey())

                    .retrieve()

                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new FileStorageException(
                                        "Supabase download failed. HTTP "
                                                + response.getStatusCode().value()
                                                + " for storagePath=" + storagePath
                                );
                            }
                    )

                    .body(byte[].class);

        } catch (RestClientResponseException ex) {

            log.error(
                    "Supabase download failed. storagePath={}, status={}, response={}",
                    storagePath,
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString()
            );

            throw new FileStorageException(
                    "Failed to download file from Supabase Storage.",
                    ex
            );

        } catch (IllegalArgumentException ex) {

            log.error(
                    "Invalid Supabase download URL. storagePath={}",
                    storagePath,
                    ex
            );

            throw new FileStorageException(
                    "Invalid Supabase Storage URL.",
                    ex
            );
        }
    }

}