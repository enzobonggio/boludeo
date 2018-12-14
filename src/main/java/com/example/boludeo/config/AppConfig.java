package com.example.boludeo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class AppConfig {
    @Bean("trx-scheduler")
    public Scheduler jdbcScheduler(
            @Value("${scheduler.jdbc.pool.size:5}") int schedulerSize,
            @Value("${scheduler.jdbc.pool.max.size:10}") int schedulerMaxSize,
            @Value("${scheduler.jdbc.pool.queue:500}") int queueCapacity) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(schedulerSize);
        taskExecutor.setMaxPoolSize(schedulerMaxSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setThreadNamePrefix("jdbc-scheduler-");
        taskExecutor.initialize();
        return Schedulers.fromExecutor(taskExecutor);
    }
}
