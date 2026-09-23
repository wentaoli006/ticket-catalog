package com.owen.application.supplier;

import com.owen.api.supplier.PullSupplierCommand;
import com.owen.domain.supplier.service.SupplierStaticPullService;
import com.owen.types.enums.SupplierEnum;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 供应商静态拉取用例。只转发命令，不解释供应商协议。
 */
@Validated
@Service
public class SupplierStaticPullAppService {

    private final SupplierStaticPullService pullService;

    public SupplierStaticPullAppService(SupplierStaticPullService pullService) {
        this.pullService = pullService;
    }

    public void pullProjects(@Valid PullSupplierCommand command) {
        pullService.pullProjects(SupplierEnum.of(command.getSupplierId()));
    }

    public void pullChildren(@Valid PullSupplierCommand command) {
        pullService.pullChildren(SupplierEnum.of(command.getSupplierId()));
    }

}
