package com.adrovis.adrovis_backend.career.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramApplicationCreateRequest {

    @NotBlank
    @Size(min = 2, max = 150)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phone;

    @NotBlank
    @Size(min = 2, max = 200)
    private String college;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer graduationYear;

    @NotNull(message = "Resume file is required")
    private MultipartFile resume;
}