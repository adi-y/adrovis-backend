package com.adrovis.adrovis_backend.contact.dto.response;

import com.adrovis.adrovis_backend.contact.enums.LeadStatus;
import com.adrovis.adrovis_backend.contact.enums.LeadType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(
        name = "LeadResponse",
        description = "Response returned for contact leads with full enterprise dossier."
)
public record LeadResponse(

        @Schema(description = "Unique identifier of the lead", example = "8c4db6f5-12b0-4dd9-9c75-1ef56a7dc5a1")
        UUID id,

        @Schema(description = "Type of lead", example = "CONSULTATION")
        LeadType leadType,

        @Schema(description = "Full name of the client/prospect", example = "Rohan Mandal")
        String fullName,

        @Schema(description = "Company or organization name", example = "Adrovis Technologies")
        String company,

        @Schema(description = "Contact email address", example = "rohan@example.com")
        String email,

        @Schema(description = "Contact phone number", example = "+91 9876543210")
        String phone,

        @Schema(description = "Project domain or category", example = "Web Development")
        String projectType,

        @Schema(description = "Estimated project budget", example = "₹1L – ₹5L")
        String budget,

        @Schema(description = "Project brief or consultation message")
        String message,

        @Schema(description = "Current lifecycle status", example = "NEW")
        LeadStatus status,

        @Schema(description = "Timestamp when the lead was created")
        Instant createdAt

) {
}
