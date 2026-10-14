package com.owen.application.supplier;

import com.owen.api.supplier.PullProjectChildrenCommand;
import com.owen.api.supplier.PullSupplierCommand;
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

    private final SupplierStaticPullService pullService;
    private final ExecutorService childPullExecutor;

    public SupplierStaticPullAppService(SupplierStaticPullService pullService,
                                         @Qualifier(CHILD_PULL_EXECUTOR) ExecutorService childPullExecutor) {
        this.pullService = pullService;
        this.childPullExecutor = childPullExecutor;
    }

    public void pullProjects(@Valid PullSupplierCommand command) {
        pullService.pullProjects(SupplierEnum.of(command.getSupplierId()));
    }

    /**
     * Job 批量拉取。每个项目一个任务，等全部结束后，有失败才抛异常。
     */
    public void pullChildren(@Valid PullSupplierCommand command) {
        SupplierEnum supplier = SupplierEnum.of(command.getSupplierId());
        List<String> projectIds = pullService.listSupplierProjectIds(supplier); //拉取所有在售项目id
        List<String> failed = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> tasks = new ArrayList<>(projectIds.size());
        for (String supplierProjectId : projectIds) {
            tasks.add(CompletableFuture.runAsync(
                    () -> pullOne(supplier, supplierProjectId, failed), childPullExecutor));
        }
        CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
        if (!failed.isEmpty()) {
            throw new AppException(ERR_CHILD_PULL, "下级拉取失败项目数: " + failed.size());
        }
    }

    /**
     * 运营手动拉取一个项目。当前线程执行完才返回，失败直接抛出。
     */
    public void pullProjectChildren(@Valid PullProjectChildrenCommand command) {
        pullService.pullProjectChildren(
                SupplierEnum.of(command.getSupplierId()), command.getSupplierProjectId());
    }

    private void pullOne(SupplierEnum supplier, String supplierProjectId, List<String> failed) {
        try {
            pullService.pullProjectChildren(supplier, supplierProjectId);
        } catch (RuntimeException ex) {
            failed.add(supplierProjectId);
            Logs.error(CHILD_PULL_TITLE, "项目下级拉取失败",
                    Map.of("supplierProjectId", supplierProjectId), ex);
        }
    }

}
