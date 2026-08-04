package com.adrovis.adrovis_backend.email.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(MailProperties.class)
public class AsyncConfigEmail {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("Email-");

        executor.initialize();

        return executor;
    }
}