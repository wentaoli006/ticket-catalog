package com.owen.api.supplier;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

/**
 * 静态拉取命令。只指定供应商，不携带对方协议字段。
 */
@Getter
public class PullSupplierCommand {

    @NotNull
    @Positive
    private final Long supplierId;

    public PullSupplierCommand(Long supplierId) {
        this.supplierId = supplierId;
    }

}
