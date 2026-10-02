package com.adrovis.adrovis_backend.unpaidinternship.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface UnpaidInternshipStorageService {

    StoredResume upload(MultipartFile file);

    Resource download(String storagePath);

    record StoredResume(
            String storagePath,
            String originalName,
            String mimeType,
            long sizeBytes
    ) {
    }
}