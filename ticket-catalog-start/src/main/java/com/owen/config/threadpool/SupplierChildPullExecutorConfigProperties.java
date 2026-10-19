package com.owen.config.threadpool;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "thread.pool.supplier.child.pull.config", ignoreInvalidFields = true)
public class SupplierChildPullExecutorConfigProperties {

    /** 核心线程数 */
    private Integer corePoolSize = 16;
    /** 最大线程数 */
    private Integer maxPoolSize = 16;
    /** 最大等待时间 */
    private Long keepAliveTime = 60L;
    /** 最大队列数 */
    private Integer blockQueueSize = 64;
    /** 单个供应商同时提交的项目数 */
    private Integer perSupplierInFlight = 4;
    /*
     * AbortPolicy：丢弃任务并抛出RejectedExecutionException异常。
     * DiscardPolicy：直接丢弃任务，但是不会抛出异常
     * DiscardOldestPolicy：将最早进入队列的任务删除，之后再尝试加入队列的任务被拒绝
     * CallerRunsPolicy：如果任务添加线程池失败，那么主线程自己执行该任务
     * */
    private String policy = "AbortPolicy";

}
