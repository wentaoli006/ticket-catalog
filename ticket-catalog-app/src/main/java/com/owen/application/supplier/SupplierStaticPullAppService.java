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
 * 单个项目的下级拉取由 pullOne 自己提交到线程池，调用方不用关心线程。
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
     * 该供应商已落库项目逐个提交。等全部结束后，有失败才抛异常。
     */
    public void pullChildren(@Valid PullSupplierCommand command) {
        SupplierEnum supplier = SupplierEnum.of(command.getSupplierId());
        List<String> projectIds = pullService.listSupplierProjectIds(supplier);
        List<String> failed = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> tasks = new ArrayList<>(projectIds.size());
        for (String supplierProjectId : projectIds) {
            tasks.add(pullOne(supplier, supplierProjectId)
                    .whenComplete((ignored, error) -> {
                        if (error != null) {
                            failed.add(supplierProjectId);
                        }
                    }));
        }
        CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
        if (!failed.isEmpty()) {
            throw new AppException(ERR_CHILD_PULL, "下级拉取失败项目数: " + failed.size());
        }
    }

    /**
     * HTTP 手动拉取。只负责提交，不等待拉取结束。
     */
    public void pullProjectChildren(@Valid PullProjectChildrenCommand command) {
        pullOne(SupplierEnum.of(command.getSupplierId()), command.getSupplierProjectId());
    }

    /**
     * 单个项目的下级拉取。调用即提交到项目拉取线程池。
     */
    public CompletableFuture<Void> pullOne(SupplierEnum supplier, String supplierProjectId) {
        return CompletableFuture.runAsync(() -> {
            try {
                pullService.pullProjectChildren(supplier, supplierProjectId);
            } catch (RuntimeException ex) {
                Logs.error(CHILD_PULL_TITLE, "项目下级拉取失败",
                        Map.of("supplierProjectId", supplierProjectId), ex);
                throw ex;
            }
        }, childPullExecutor);
    }

}
