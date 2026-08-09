package com.adrovis.adrovis_backend.interview.dto.response;


import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilitySlotResponse(
        LocalDate availableDate,
        LocalTime startTime,
        LocalTime endTime,
        String timezone
) {}
