package com.adrovis.adrovis_backend.interview.service.impl;

import com.adrovis.adrovis_backend.common.exception.FileStorageException;
import com.adrovis.adrovis_backend.interview.service.ResumeTextExtractorService;
import com.adrovis.adrovis_backend.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeTextExtractorServiceImpl
        implements ResumeTextExtractorService {

    private final FileStorageService fileStorageService;

    private final Tika tika = new Tika();

    @Override
    public String extractText(String storageKey) {

        log.info(
                "Starting resume text extraction. storageKey={}",
                storageKey
        );

        try {

            Resource resource = fileStorageService.read(storageKey);

            if (resource == null || !resource.exists()) {
                throw new FileStorageException(
                        "Resume file not found."
                );
            }

            log.info(
                    "Resume resource obtained successfully. storageKey={}, filename={}",
                    storageKey,
                    resource.getFilename()
            );

            try (InputStream inputStream = resource.getInputStream()) {

                String text = tika.parseToString(inputStream);

                if (text == null || text.isBlank()) {
                    throw new FileStorageException(
                            "Resume contains no readable text."
                    );
                }

                log.info(
                        "Tika extraction successful. storageKey={}, characters={}",
                        storageKey,
                        text.length()
                );

                return text.trim();
            }

        } catch (FileStorageException ex) {

            log.error(
                    "Resume extraction failed. storageKey={}, reason={}",
                    storageKey,
                    ex.getMessage()
            );

            throw ex;

        } catch (Exception ex) {

            log.error(
                    "Unexpected error during resume extraction. storageKey={}",
                    storageKey,
                    ex
            );

            throw new FileStorageException(
                    "Failed to extract text from resume.",
                    ex
            );
        }
    }
}