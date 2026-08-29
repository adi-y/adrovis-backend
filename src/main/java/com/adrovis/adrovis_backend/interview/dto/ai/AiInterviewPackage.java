package com.adrovis.adrovis_backend.interview.dto.ai;

import java.util.List;

public record AiInterviewPackage(
        List<AiInterviewQuestion> questions
) {
}