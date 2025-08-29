package com.oa.framework.config;

import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component("applicationEventMulticaster")
public class OaMulticaster extends SimpleApplicationEventMulticaster {

    public OaMulticaster() {
        // 最大可创建的线程数
        int maxPoolSize = 200;
        // 核心线程池大小
        int corePoolSize = 50;
        // 队列最大长度
        int queueCapacity = 1000;
        // 线程池维护线程所允许的空闲时间
        int keepAliveSeconds = 300;
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueCapacity), new ThreadPoolExecutor.CallerRunsPolicy());
        setTaskExecutor(threadPoolExecutor);
    }
}
