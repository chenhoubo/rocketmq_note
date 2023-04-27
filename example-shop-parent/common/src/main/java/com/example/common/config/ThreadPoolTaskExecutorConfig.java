package com.example.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/27 14:28
 * @Desc
 * @since seeingflow
 */
@Component
public class ThreadPoolTaskExecutorConfig {
    @Bean
    public ThreadPoolTaskExecutor getThreadPool(){

        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);

        executor.setMaxPoolSize(8);

        executor.setQueueCapacity(100);

        executor.setKeepAliveSeconds(60);

        executor.setThreadNamePrefix("Pool-A");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        return executor;

    }
}
