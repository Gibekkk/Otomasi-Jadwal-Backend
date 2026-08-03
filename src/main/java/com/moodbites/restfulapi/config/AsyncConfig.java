package com.moodbites.restfulapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "asyncExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // Set the core pool size
        executor.setMaxPoolSize(10); // Set the maximum pool size
        executor.setQueueCapacity(25); // Set the capacity of the task queue
        executor.setThreadNamePrefix("custom-async-"); // Set the thread name prefix
        executor.initialize();
        return executor;
    }
}