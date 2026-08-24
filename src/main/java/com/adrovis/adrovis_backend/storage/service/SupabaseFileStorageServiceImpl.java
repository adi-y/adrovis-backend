package com.adrovis.adrovis_backend.storage.service;

import com.adrovis.adrovis_backend.common.exception.FileStorageException;
import com.adrovis.adrovis_backend.config.StorageProperties;
import com.adrovis.adrovis_backend.storage.client.SupabaseStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
@Service
@Profile({"dev", "prod"})
@Slf4j
public class SupabaseFileStorageServiceImpl extends AbstractFileStorageService {

    private final SupabaseStorageClient storageClient;

    public SupabaseFileStorageServiceImpl(
            StorageProperties storageProperties,
            SupabaseStorageClient storageClient
    ) {
        super(storageProperties);
        this.storageClient = storageClient;
    }

    @Override
    protected String store(
            String storageKey,
            MultipartFile file
    ) {

        try {

            storageClient.upload(
                    storageKey,
                    file.getBytes(),
                    file.getContentType()
            );

            return storageClient.getPublicUrl(storageKey);

        } catch (IOException ex) {

            throw new FileStorageException(
                    "Failed to upload file to Supabase Storage.",
                    ex
            );
        }
    }

    @Override
    public void delete(String storageKey) {

        storageClient.delete(storageKey);
    }

    @Override
    public Resource read(String storageKey) {

        try {
            log.info(
                    "Reading resume from Supabase Storage. storageKey={}",
                    storageKey
            );

            byte[] bytes = storageClient.download(storageKey);

            if (bytes == null || bytes.length == 0) {
                throw new FileStorageException(
                        "Stored file is empty: " + storageKey
                );
            }

            log.info(
                    "Resume downloaded from Supabase successfully. storageKey={}, bytes={}",
                    storageKey,
                    bytes.length
            );

            return new ByteArrayResource(bytes);

        } catch (FileStorageException ex) {
            throw ex;

        } catch (Exception ex) {

            log.error(
                    "Failed to read resume from Supabase. storageKey={}",
                    storageKey,
                    ex
            );

            throw new FileStorageException(
                    "Failed to read stored file from Supabase.",
                    ex
            );
        }
    }

}