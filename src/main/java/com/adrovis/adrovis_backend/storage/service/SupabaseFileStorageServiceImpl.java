package com.adrovis.adrovis_backend.storage.service;

import com.adrovis.adrovis_backend.common.exception.FileStorageException;
import com.adrovis.adrovis_backend.config.StorageProperties;
import com.adrovis.adrovis_backend.storage.client.SupabaseStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Profile("prod")
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

}