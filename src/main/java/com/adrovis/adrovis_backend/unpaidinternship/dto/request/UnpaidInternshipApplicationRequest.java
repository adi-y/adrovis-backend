package com.adrovis.adrovis_backend.unpaidinternship.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UnpaidInternshipApplicationRequest {

    @NotBlank
    @Size(min = 2, max = 150)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = "\\d{10}")
    private String phone;

    @NotBlank
    @Size(min = 2, max = 200)
    private String college;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer graduationYear;

    @Size(max = 100)
    private String batch;

    @NotNull
    private MultipartFile resume;
}