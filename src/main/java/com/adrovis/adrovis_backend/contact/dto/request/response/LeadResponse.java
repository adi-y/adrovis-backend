package com.adrovis.adrovis_backend.contact.dto.response;

import com.adrovis.adrovis_backend.contact.enums.LeadStatus;
import com.adrovis.adrovis_backend.contact.enums.LeadType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing a successfully created lead.
 *
 * <p>
 * This DTO is intended for internal service/controller communication
 * and future Admin APIs. It deliberately avoids exposing the JPA entity.
 * </p>
 */
@Builder
@Schema(
        name = "LeadResponse",
        description = "Response returned after successfully creating a lead."
)
public record LeadResponse(

        @Schema(
                description = "Unique identifier of the lead",
                example = "8c4db6f5-12b0-4dd9-9c75-1ef56a7dc5a1"
        )
        UUID id,

        @Schema(
                description = "Type of lead",
                example = "CALLBACK"
        )
        LeadType leadType,

        @Schema(
                description = "Current lifecycle status",
                example = "NEW"
        )
        LeadStatus status,

        @Schema(
                description = "Timestamp when the lead was created"
        )
        Instant createdAt

) {
}