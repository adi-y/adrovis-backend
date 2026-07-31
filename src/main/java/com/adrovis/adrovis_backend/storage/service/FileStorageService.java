package com.adrovis.adrovis_backend.storage.service;

import com.adrovis.adrovis_backend.storage.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse upload(MultipartFile file);

    void delete(String storageKey);
}