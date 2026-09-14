package com.adrovis.adrovis_backend.program.controller;

import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import com.adrovis.adrovis_backend.program.dto.request.UpdateProgramConfigurationRequest;
import com.adrovis.adrovis_backend.program.dto.response.ProgramConfigurationResponse;
import com.adrovis.adrovis_backend.program.entity.ProgramConfiguration;
import com.adrovis.adrovis_backend.program.service.ProgramConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/programs")
public class ProgramConfigurationController {

    private static final String PROGRAM_KEY =
            "SOFTWARE_DEVELOPER_INTERNSHIP";

    private final ProgramConfigurationService service;

    @GetMapping("/software-developer-internship")
    public ResponseEntity<
            ApiResponse<ProgramConfigurationResponse>
            > getPublicConfiguration() {

        ProgramConfiguration configuration =
                service.getActive(PROGRAM_KEY);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Program configuration retrieved successfully.",
                        ProgramConfigurationResponse.from(configuration)
                )
        );
    }

    @PutMapping("/admin/software-developer-internship")
    public ResponseEntity<
            ApiResponse<ProgramConfigurationResponse>
            > updateConfiguration(
            @Valid
            @RequestBody
            UpdateProgramConfigurationRequest request
    ) {

        ProgramConfiguration configuration =
                service.update(
                        PROGRAM_KEY,
                        request.feeEnabled(),
                        request.feeAmount(),
                        request.currency()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Program configuration updated successfully.",
                        ProgramConfigurationResponse.from(configuration)
                )
        );
    }
}