package com.adrovis.adrovis_backend.career.validation;

import com.adrovis.adrovis_backend.career.dto.request.CreateApplicationRequest;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ApplicationRequestValidator
        implements ConstraintValidator<ValidApplicationRequest, CreateApplicationRequest> {

    @Override
    public boolean isValid(
            CreateApplicationRequest request,
            ConstraintValidatorContext context
    ) {

        if (request == null) {
            return true;
        }

        if (request.applicationType() == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (request.applicationType() == ApplicationType.JOB
                && request.jobId() == null) {

            context.buildConstraintViolationWithTemplate(
                            "Job ID is required for JOB applications."
                    )
                    .addPropertyNode("jobId")
                    .addConstraintViolation();

            return false;
        }

        if (request.applicationType() == ApplicationType.PROGRAM
                && request.jobId() != null) {

            context.buildConstraintViolationWithTemplate(
                            "Job ID must be null for PROGRAM applications."
                    )
                    .addPropertyNode("jobId")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}