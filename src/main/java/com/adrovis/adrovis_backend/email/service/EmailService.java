package com.adrovis.adrovis_backend.email.service;

import com.adrovis.adrovis_backend.career.entity.Application;

public interface EmailService {

    void sendApplicationReceivedEmailAsync(Application application);

}