package com.adrovis.adrovis_backend.email.service;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.interview.entity.Interview;

public interface EmailService {

    void sendApplicationReceivedEmailAsync(Application application);

    void sendApplicationShortlistedEmailAsync(Application application);

    void sendApplicationRejectedEmailAsync(Application application);

    void sendInterviewAvailabilityRequestEmailAsync(Application application);

    void sendInterviewScheduledEmailAsync(Application application, Interview interview);

    void sendInterviewRescheduledEmailAsync(Application application, Interview interview);

    void sendInterviewCancelledEmailAsync(Application application, Interview interview);

}