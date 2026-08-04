package com.adrovis.adrovis_backend.common.entity;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApplicationIdGenerator {

    private final ApplicationRepository applicationRepository;

    public String next() {

        int year = Year.now().getValue();
        String prefix = "APP" + year;

        Optional<Application> latest =
                applicationRepository.findTopByOrderByApplicationIdDesc();

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