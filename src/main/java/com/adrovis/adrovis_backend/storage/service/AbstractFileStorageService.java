package com.adrovis.adrovis_backend.storage.service;

import com.adrovis.adrovis_backend.common.exception.FileValidationException;
import com.adrovis.adrovis_backend.config.StorageProperties;
import com.adrovis.adrovis_backend.storage.dto.response.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class AbstractFileStorageService
        implements FileStorageService {

    protected final StorageProperties storageProperties;

    @Override
    public FileUploadResponse upload(MultipartFile file) {

        validate(file);

        String originalFilename = file.getOriginalFilename();

        if (!StringUtils.hasText(originalFilename)) {
            throw new FileValidationException(
                    "File validation failed.",
                    Map.of(
                            "resume",
                            "Uploaded file must have a valid filename."
                    )
            );
        }

        String extension = getExtension(originalFilename);

        String filename = UUID.randomUUID() + extension;

        YearMonth yearMonth = YearMonth.now();

        String storageKey =
                "resumes/%d/%02d/%s".formatted(
                        yearMonth.getYear(),
                        yearMonth.getMonthValue(),
                        filename
                );

        String fileUrl = store(
                storageKey,
                file
        );

        return new FileUploadResponse(
                storageKey,
                fileUrl,
                originalFilename,
                file.getContentType(),
                file.getSize()
        );
    }

    /**
     * Store file in the underlying provider.
     * Returns the public URL.
     */
    protected abstract String store(
            String storageKey,
            MultipartFile file
    );

    protected void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileValidationException(
                    "File validation failed.",
                    Map.of(
                            "resume",
                            "Resume file is required."
                    )
            );
        }

        if (file.getSize() >
                storageProperties.getMaxFileSize().toBytes()) {

            throw new FileValidationException(
                    "File validation failed.",
                    Map.of(
                            "resume",
                            "Uploaded file exceeds the maximum allowed size."
                    )
            );
        }

        if (!storageProperties.getAllowedTypes()
                .contains(file.getContentType())) {

            throw new FileValidationException(
                    "File validation failed.",
                    Map.of(
                            "resume",
                            "Only PDF files are allowed."
                    )
            );
        }
    }

    protected String getExtension(String filename) {

        String extension =
                StringUtils.getFilenameExtension(filename);

        return extension == null ? "" : "." + extension;
    }

}