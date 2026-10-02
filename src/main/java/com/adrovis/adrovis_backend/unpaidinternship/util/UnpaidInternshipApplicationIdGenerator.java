package com.adrovis.adrovis_backend.unpaidinternship.util;

import com.adrovis.adrovis_backend.unpaidinternship.entity.UnpaidInternshipApplication;
import com.adrovis.adrovis_backend.unpaidinternship.repository.UnpaidInternshipApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UnpaidInternshipApplicationIdGenerator {

    private final UnpaidInternshipApplicationRepository repository;

    public String next() {

        int year = Year.now().getValue();
        String prefix = "APP" + year;

        Optional<UnpaidInternshipApplication> latest =
                repository.findTopByOrderByApplicationIdDesc();

        long nextSequence = 1;

        if (latest.isPresent()) {
            String lastId = latest.get().getApplicationId();

            if (lastId != null && lastId.startsWith(prefix)) {
                nextSequence =
                        Long.parseLong(lastId.substring(prefix.length())) + 1;
            }
        }

        return prefix + String.format("%05d", nextSequence);
    }
}