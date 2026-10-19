package com.owen.config.threadpool;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableConfigurationProperties(SupplierChildPullExecutorConfigProperties.class)
public class SupplierChildPullExecutorConfig {

    public static final String BEAN_NAME = "supplierChildPullExecutor";

    /**
     * 4 个实例，每个供应商的 Job 由 XXL-JOB 分到其中一个。猫眼这类供应商在售项目可以上千，队列不能按项目数设。
     * 拉取是 IO，core 等于 max 停在 16，不跟 Hikari 的 15 条连接抢。core 小于 max 时队列会把多余线程挡住。
     * 队列 64，只做交接缓冲。分片广播后单节点同时只提交 4 个，8 个分片合计不超过 32 路。
     * AbortPolicy 在池子关闭或队列真实打满时失败，不静默丢项目。
     */
    @Bean(name = BEAN_NAME, destroyMethod = "shutdown")
    public ExecutorService supplierChildPullExecutor(SupplierChildPullExecutorConfigProperties properties) {
        if (properties.getCorePoolSize() > properties.getMaxPoolSize()) {
            throw new IllegalArgumentException("corePoolSize 不能大于 maxPoolSize");
        }
        if (properties.getPerSupplierInFlight() < 1
                || properties.getPerSupplierInFlight() > properties.getMaxPoolSize()) {
            throw new IllegalArgumentException("perSupplierInFlight 必须在 1 和 maxPoolSize 之间");
        }
        // 实例化策略
        RejectedExecutionHandler handler;
        switch (properties.getPolicy()) {
            case "AbortPolicy":
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }
        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(properties.getBlockQueueSize()),
                namedFactory(),
                handler);
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
