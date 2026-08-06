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

            restClient.post()

                    .uri(properties.getUrl()
                            + "/storage/v1/object/"
                            + properties.getBucket()
                            + "/"
                            + storagePath)

                    .header("apikey", properties.getServiceKey())
                    .header("Authorization",
                            "Bearer " + properties.getServiceKey())
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
                    ex.getResponseBodyAsString()
            );

            throw new FileStorageException(
                    "Failed to upload file to Supabase Storage.",
                    ex
            );
        }
    }

    public void delete(String storagePath) {

        try {

            restClient.delete()

                    .uri(properties.getUrl()
                            + "/storage/v1/object/"
                            + properties.getBucket()
                            + "/"
                            + storagePath)

                    .header("apikey", properties.getServiceKey())

                    .header("Authorization",
                            "Bearer " + properties.getServiceKey())

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
                    ex.getResponseBodyAsString()
            );

            throw new FileStorageException(
                    "Failed to delete file from Supabase Storage.",
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
}