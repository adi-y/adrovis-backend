package com.adrovis.adrovis_backend.storage.service;

import com.adrovis.adrovis_backend.common.exception.FileStorageException;
import com.adrovis.adrovis_backend.common.exception.FileValidationException;
import com.adrovis.adrovis_backend.config.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Profile("local")
@Slf4j
public class LocalFileStorageServiceImpl
        extends AbstractFileStorageService {

    public LocalFileStorageServiceImpl(
            StorageProperties storageProperties
    ) {
        super(storageProperties);
    }

    @Override
    protected String store(
            String storageKey,
            MultipartFile file
    ) {

        try {

            Path destination = Path.of(
                    storageProperties.getUploadDir(),
                    storageKey
            );

            Files.createDirectories(
                    destination.getParent()
            );

            Files.copy(
                    file.getInputStream(),
                    destination,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/" +
                    storageKey.replace("\\", "/");

        } catch (IOException ex) {

            throw new FileStorageException(
                    "Failed to store uploaded file.",
                    ex
            );
        }
    }

    @Override
    public Resource read(String storageKey) {

        try {

            Path root = Path.of(
                    storageProperties.getUploadDir()
            ).toAbsolutePath().normalize();

            Path file = root
                    .resolve(storageKey)
                    .normalize();

            // Prevent path traversal
            if (!file.startsWith(root)) {
                throw new FileValidationException(
                        "File validation failed.",
                        java.util.Map.of(
                                "resume",
                                "Invalid storage path."
                        )
                );
            }

            if (!Files.exists(file) ||
                    !Files.isRegularFile(file)) {

                throw new FileStorageException(
                        "Stored file not found."
                );
            }

            return new FileSystemResource(file);

        } catch (
                FileStorageException |
                FileValidationException ex
        ) {

            throw ex;

        }
    }

    @Override
    public void delete(String storageKey) {

        try {

            Path root = Path.of(
                    storageProperties.getUploadDir()
            ).toAbsolutePath().normalize();

            Path file = root
                    .resolve(storageKey)
                    .normalize();

            if (!file.startsWith(root)) {
                throw new FileValidationException(
                        "File validation failed.",
                        java.util.Map.of(
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
}