package com.adrovis.adrovis_backend.common.entity;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ApplicationIdGenerator {

    private final AtomicLong counter = new AtomicLong(1);

    public String next() {
        int year = Year.now().getValue();
        long seq = counter.getAndIncrement();
        return String.format("APP%d%05d", year, seq);
    }
}