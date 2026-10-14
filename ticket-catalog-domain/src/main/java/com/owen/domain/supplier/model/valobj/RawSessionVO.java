package com.owen.domain.supplier.model.valobj;

import com.owen.types.enums.SaleStatusEnum;

import java.time.LocalDateTime;

/**
 * 供应商原始场次。时间都是项目时区下的钟面时间。
 */
public record RawSessionVO(
        String supplierSessionId,
        String name,
        LocalDateTime sessionStartTime,
        LocalDateTime sessionEndTime,
        LocalDateTime saleStartTime,
        LocalDateTime saleEndTime,
        boolean realName,
        boolean seatSelection,
        SaleStatusEnum status,
        int maxSaleQty
) {

    public RawSessionVO {
        if (supplierSessionId == null || supplierSessionId.isBlank()) {
            throw new IllegalArgumentException("供应商场次ID不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("场次名称不能为空");
        }
        if (sessionStartTime == null) {
            throw new IllegalArgumentException("场次开始时间不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("场次状态不能为空");
        }
        if (maxSaleQty < 1) {
            throw new IllegalArgumentException("最大售卖数量至少为 1");
        }
    }

}
