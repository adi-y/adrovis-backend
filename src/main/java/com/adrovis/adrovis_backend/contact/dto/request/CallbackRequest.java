package com.adrovis.adrovis_backend.contact.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request payload for the "Request Callback" endpoint.
 *
 * <p>This DTO represents the minimum information required for the
 * sales team to initiate a callback.</p>
 */
@Builder
@Schema(
        name = "CallbackRequest",
        description = "Request payload for creating a callback lead."
)
public record CallbackRequest(

        @Schema(
                description = "Full name of the requester",
                example = "Rahul Sharma"
        )
        @NotBlank(message = "Full name is required.")
        @Size(
                min = 2,
                max = 150,
                message = "Full name must be between 2 and 150 characters."
        )
        String fullName,

        @Schema(
                description = "Email address",
                example = "rahul@gmail.com"
        )
        @NotBlank(message = "Email is required.")
        @Email(message = "Please provide a valid email address.")
        @Size(
                max = 150,
                message = "Email must not exceed 150 characters."
        )
        String email,

        @Schema(
                description = "10-digit mobile number",
                example = "9876543210"
        )
        @NotBlank(message = "Phone number is required.")
        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "Phone number must contain exactly 10 digits."
        )
        String phone

) {
}