package com.adrovis.adrovis_backend.storage.service;

import com.adrovis.adrovis_backend.storage.dto.response.FileUploadResponse;
import com.adrovis.adrovis_backend.config.StorageProperties;
import com.adrovis.adrovis_backend.common.exception.FileStorageException;
import com.adrovis.adrovis_backend.common.exception.FileValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.time.YearMonth;
import java.util.Map;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final StorageProperties storageProperties;

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

        try {

            String extension = getExtension(originalFilename);

            String filename = UUID.randomUUID() + extension;

            YearMonth yearMonth = YearMonth.now();

            Path directory = Path.of(
                    storageProperties.getUploadDir(),
                    "resumes",
                    String.valueOf(yearMonth.getYear()),
                    String.format("%02d", yearMonth.getMonthValue())
            );

            Files.createDirectories(directory);

            Path destination = directory.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    destination
            );

            String storageKey = Path.of(
                    "resumes",
                    String.valueOf(yearMonth.getYear()),
                    String.format("%02d", yearMonth.getMonthValue()),
                    filename
            ).toString().replace("\\", "/");

            String fileUrl = "/uploads/" + storageKey;

            return new FileUploadResponse(
                    storageKey,
                    fileUrl,
                    originalFilename,
                    file.getContentType(),
                    file.getSize()
            );

        } catch (IOException ex) {

            throw new FileStorageException(
                    "Failed to store uploaded file.",
                    ex
            );
        }
    }

    @Override
    public void delete(String storageKey) {

        try {

            Path root = Path.of(storageProperties.getUploadDir())
                    .toAbsolutePath()
                    .normalize();

            Path file = root.resolve(storageKey).normalize();

            if (!file.startsWith(root)) {
                throw new FileValidationException(
                        "File validation failed.",
                        Map.of(
                                "resume",
                                "Invalid storage path."
                        )
                );
            }

            Files.deleteIfExists(file);

        } catch (IOException ex) {

            throw new FileStorageException(
                    "Failed to delete stored file.",
                    ex
            );
        }
    }

    private void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileValidationException(
                    "File validation failed.",
                    Map.of(
                            "resume",
                            "Resume file is required."
                    )
            );
        }

        if (file.getSize() > storageProperties.getMaxFileSize().toBytes()) {
            throw new FileValidationException(
                    "File validation failed.",
                    Map.of(
                            "resume",
                            "Uploaded file exceeds the maximum allowed size."
                    )
            );
        }

        if (!storageProperties.getAllowedTypes().contains(file.getContentType())) {
            throw new FileValidationException(
                    "File validation failed.",
                    Map.of(
                            "resume",
                            "Only PDF files are allowed."
                    )
            );
        }
    }

    private String getExtension(String filename) {

        String extension = StringUtils.getFilenameExtension(filename);

        return extension == null ? "" : "." + extension;
    }
}