package com.adrovis.adrovis_backend.career.repository;

import com.adrovis.adrovis_backend.career.entity.Job;
import com.adrovis.adrovis_backend.career.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findAllByStatus(JobStatus status);

}