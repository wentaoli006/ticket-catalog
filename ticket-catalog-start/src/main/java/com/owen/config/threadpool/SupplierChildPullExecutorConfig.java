package com.owen.config.threadpool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 供应商项目下级拉取线程池。一个任务是一个项目，和通用线程池分开。
 * 队列满时由调用线程自己跑，避免定时任务把项目静默丢掉。
 */
@Configuration
public class SupplierChildPullExecutorConfig {

    public static final String BEAN_NAME = "supplierChildPullExecutor";

    private static final int CORE_POOL_SIZE = 8;
    private static final int MAX_POOL_SIZE = 16;
    private static final int QUEUE_CAPACITY = 200;
    private static final long KEEP_ALIVE_SECONDS = 60L;

    @Bean(name = BEAN_NAME, destroyMethod = "shutdown")
    public ExecutorService supplierChildPullExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                namedFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    private static ThreadFactory namedFactory() {
        AtomicInteger seq = new AtomicInteger(1);
        return task -> {
            Thread thread = new Thread(task, "supplier-child-pull-" + seq.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
    }

}
