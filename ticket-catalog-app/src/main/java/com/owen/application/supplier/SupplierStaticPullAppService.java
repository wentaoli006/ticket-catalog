package com.owen.application.supplier;

import com.owen.api.supplier.PullProjectChildrenCommand;
import com.owen.api.supplier.PullSupplierCommand;
import com.owen.domain.adapter.IDynamicConfig;
import com.owen.domain.supplier.service.SupplierStaticPullService;
import com.owen.types.enums.SupplierEnum;
import com.owen.types.exception.AppException;
import com.owen.types.log.Logs;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

/**
 * 供应商静态拉取用例。只转发命令，不解释供应商协议。
 * 线程池只用于 Job 批量拉下级。手动拉一个项目在当前线程执行。
 */
@Validated
@Service
public class SupplierStaticPullAppService {

    private static final String CHILD_PULL_TITLE = "拉取供应商项目下级";
    private static final String CHILD_PULL_EXECUTOR = "supplierChildPullExecutor";
    private static final String ERR_CHILD_PULL = "SUPPLIER_CHILD_PULL_FAILED";
    private static final String IN_FLIGHT_KEY = "thread.pool.supplier.child.pull.config.per-supplier-in-flight";

    private final SupplierStaticPullService pullService;
    private final ExecutorService childPullExecutor;
    private final IDynamicConfig dynamicConfig;

    public SupplierStaticPullAppService(SupplierStaticPullService pullService,
                                         @Qualifier(CHILD_PULL_EXECUTOR) ExecutorService childPullExecutor,
                                         IDynamicConfig dynamicConfig) {
        this.pullService = pullService;
        this.childPullExecutor = childPullExecutor;
        this.dynamicConfig = dynamicConfig;
    }

    public void pullProjects(@Valid PullSupplierCommand command) {
        pullService.pullProjects(SupplierEnum.of(command.getSupplierId()));
    }

    /**
     * Job 批量拉取。每个项目一个任务，等全部结束后，有失败才抛异常。
     * 同时只提交 perSupplierInFlight 个，一个跑完再补下一个。
     */
    public void pullChildren(@Valid PullSupplierCommand command) {
        SupplierEnum supplier = SupplierEnum.of(command.getSupplierId());
        List<Long> projectIds = pullService.listProjectIds(
                supplier, command.getShardIndex(), command.getShardTotal());
        List<Long> failed = Collections.synchronizedList(new ArrayList<>());
        int inFlightLimit = dynamicConfig.getInt(IN_FLIGHT_KEY, 4);
        Semaphore inFlight = new Semaphore(inFlightLimit);
        for (Long projectId : projectIds) {
            inFlight.acquireUninterruptibly();
            try {
                CompletableFuture.runAsync(() -> {
                    try {
                        pullOne(projectId, failed);
                    } finally {
                        inFlight.release();
                    }
                }, childPullExecutor);
            } catch (RuntimeException ex) {
                inFlight.release();
                throw ex;
            }
        }
        inFlight.acquireUninterruptibly(inFlightLimit);
        if (!failed.isEmpty()) {
            throw new AppException(ERR_CHILD_PULL, "下级拉取失败项目数: " + failed.size());
        }
    }

    /**
     * 运营手动拉取一个项目。当前线程执行完才返回，失败直接抛出。
     */
    public void pullProjectChildren(@Valid PullProjectChildrenCommand command) {
        pullService.pullProjectChildren(command.getProjectId());
    }

    private void pullOne(Long projectId, List<Long> failed) {
        try {
            pullService.pullProjectChildren(projectId);
        } catch (RuntimeException ex) {
            failed.add(projectId);
            Logs.error(CHILD_PULL_TITLE, "项目下级拉取失败",
                    Map.of("projectId", projectId), ex);
        }
    }

}
