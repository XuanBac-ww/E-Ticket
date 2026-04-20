package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor(
            @Value("${app.async.mail.core-pool-size:2}") int corePoolSize,
            @Value("${app.async.mail.max-pool-size:4}") int maxPoolSize,
            @Value("${app.async.mail.queue-capacity:200}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("mail-async-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ticketQrExecutor")
    public Executor ticketQrExecutor(
            @Value("${app.async.ticket-qr.core-pool-size:2}") int corePoolSize,
            @Value("${app.async.ticket-qr.max-pool-size:4}") int maxPoolSize,
            @Value("${app.async.ticket-qr.queue-capacity:200}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("ticket-qr-");
        executor.initialize();
        return executor;
    }
}
