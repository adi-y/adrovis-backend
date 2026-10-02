package com.adrovis.adrovis_backend.unpaidinternship.service.impl;

import com.adrovis.adrovis_backend.common.exception.FileStorageException;
import com.adrovis.adrovis_backend.unpaidinternship.config.UnpaidInternshipStorageProperties;
import com.adrovis.adrovis_backend.unpaidinternship.service.UnpaidInternshipStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnpaidInternshipStorageServiceImpl
        implements UnpaidInternshipStorageService {

    private final UnpaidInternshipStorageProperties properties;

    private final RestClient restClient =
            RestClient.builder().build();

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024;

    private static final String PDF =
            "application/pdf";

    @Override
    public StoredResume upload(MultipartFile file) {

        validate(file);

        String originalName =
                StringUtils.cleanPath(
                        file.getOriginalFilename() == null
                                ? "resume.pdf"
                                : file.getOriginalFilename()
                );

        String storagePath =
                "resumes/"
                        + UUID.randomUUID()
                        + ".pdf";

        try {

            String uploadUrl =
                    properties.getUrl()
                            + "/storage/v1/object/"
                            + properties.getBucket()
                            + "/"
                            + storagePath;

            restClient.post()
                    .uri(URI.create(uploadUrl))
                    .header(
                            "apikey",
                            properties.getServiceKey()
                    )
                    .header(
                            "Authorization",
                            "Bearer "
                                    + properties.getServiceKey()
                    )
                    .header("x-upsert", "false")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(file.getBytes()))
                    .retrieve()
                    .toBodilessEntity();

            return new StoredResume(
                    storagePath,
                    originalName,
                    PDF,
                    file.getSize()
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to upload unpaid internship resume.",
                    ex
            );

            throw new FileStorageException(
                    "Failed to upload resume.",
                    ex
            );
        }
    }

    @Override
    public Resource download(String storagePath) {

        try {

            String downloadUrl =
                    properties.getUrl()
                            + "/storage/v1/object/"
                            + properties.getBucket()
                            + "/"
                            + storagePath;

            byte[] bytes =
                    restClient.get()
                            .uri(URI.create(downloadUrl))
                            .header(
                                    "apikey",
                                    properties.getServiceKey()
                            )
                            .header(
                                    "Authorization",
                                    "Bearer "
                                            + properties.getServiceKey()
                            )
                            .retrieve()
                            .body(byte[].class);

            if (bytes == null || bytes.length == 0) {
                throw new FileStorageException(
                        "Stored resume is empty."
                );
            }

            return new ByteArrayResource(bytes);

        } catch (RestClientResponseException ex) {

            log.error(
                    "Failed to download unpaid internship resume. HTTP={}",
                    ex.getStatusCode().value(),
                    ex
            );

            throw new FileStorageException(
                    "Failed to download resume.",
                    ex
            );

        } catch (FileStorageException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new FileStorageException(
                    "Failed to download resume.",
                    ex
            );
        }
    }

    private void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileStorageException(
                    "Resume is required."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException(
                    "Resume must not exceed 5MB."
            );
        }

        String contentType = file.getContentType();

        if (!PDF.equalsIgnoreCase(contentType)) {
            throw new FileStorageException(
                    "Only PDF resumes are allowed."
            );
        }
    }
}