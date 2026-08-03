package com.adrovis.adrovis_backend.storage.dto.response;

public record FileUploadResponse(

        String storageKey,

        String fileUrl,

        String originalName,

        String mimeType,

        Long sizeBytes

) {
}