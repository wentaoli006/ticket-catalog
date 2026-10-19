package com.owen.api.supplier;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

/**
 * 静态拉取命令。只指定供应商，不携带对方协议字段。
 * 分片号只用于下级拉取。轮询路由时分片总数是 1，取模后等于全量。
 */
@Getter
public class PullSupplierCommand {

    @NotNull
    @Positive
    private final Long supplierId;

    @NotNull
    @PositiveOrZero
    private final Integer shardIndex;

    @NotNull
    @Positive
    private final Integer shardTotal;

    public PullSupplierCommand(Long supplierId) {
        this(supplierId, 0, 1);
    }

    public PullSupplierCommand(Long supplierId, Integer shardIndex, Integer shardTotal) {
        this.supplierId = supplierId;
        this.shardIndex = shardIndex;
        this.shardTotal = shardTotal;
    }

}
