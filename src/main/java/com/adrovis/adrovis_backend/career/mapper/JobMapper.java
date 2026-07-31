package com.adrovis.adrovis_backend.career.mapper;

import com.adrovis.adrovis_backend.career.dto.request.CreateJobRequest;
import com.adrovis.adrovis_backend.career.dto.request.UpdateJobRequest;
import com.adrovis.adrovis_backend.career.dto.response.JobResponse;
import com.adrovis.adrovis_backend.career.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface JobMapper {

    Job toEntity(CreateJobRequest request);

    JobResponse toResponse(Job job);

    void updateEntity(
            UpdateJobRequest request,
            @MappingTarget Job job
    );
}