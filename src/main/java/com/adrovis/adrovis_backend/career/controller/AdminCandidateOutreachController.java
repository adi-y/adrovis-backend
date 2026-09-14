package com.adrovis.adrovis_backend.career.controller;

import com.adrovis.adrovis_backend.career.dto.request.CandidateOutreachRequest;
import com.adrovis.adrovis_backend.career.service.CandidateOutreachService;
import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/candidate-outreach")
@RequiredArgsConstructor
public class AdminCandidateOutreachController {

    private final CandidateOutreachService candidateOutreachService;

    @PostMapping("/send")
    public ResponseEntity<
            ApiResponse<Map<String, Integer>>
            > send(
            @Valid
            @RequestBody
            CandidateOutreachRequest request
    ) {

        Map<String, Integer> result =
                candidateOutreachService.send(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Candidate outreach processed successfully.",
                        result
                )
        );
    }
}