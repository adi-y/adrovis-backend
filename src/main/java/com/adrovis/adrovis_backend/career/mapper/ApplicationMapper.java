package com.adrovis.adrovis_backend.career.mapper;

import com.adrovis.adrovis_backend.career.dto.response.ApplicationCreatedResponse;
import com.adrovis.adrovis_backend.career.dto.response.ApplicationResponse;
import com.adrovis.adrovis_backend.career.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "jobId", source = "job.id")
    ApplicationResponse toResponse(Application application);


    ApplicationCreatedResponse toCreatedResponse(Application application);
}