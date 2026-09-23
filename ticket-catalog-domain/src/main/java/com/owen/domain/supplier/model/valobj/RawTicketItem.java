package com.owen.domain.supplier.model.valobj;

import com.owen.types.dp.Money;
import com.owen.types.enums.SaleStatusEnum;

/**
 * 供应商原始票品。stockShowNum 只是展示库存，本系统不扣减。
 */
public record RawTicketItem(
        String supplierTicketItemId,
        String name,
        String description,
        int minSaleQty,
        int maxSaleQty,
        Money price,
        int stockShowNum,
        String plu,
        SaleStatusEnum status,
        int personCount
) {

    public RawTicketItem {
        if (supplierTicketItemId == null || supplierTicketItemId.isBlank()) {
            throw new IllegalArgumentException("供应商票品ID不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("票品名称不能为空");
        }
        if (price == null) {
            throw new IllegalArgumentException("票品价格不能为空");
        }
        if (minSaleQty < 1 || maxSaleQty < minSaleQty) {
            throw new IllegalArgumentException("售卖数量不合法");
        }
        if (stockShowNum < 0) {
            throw new IllegalArgumentException("展示库存不能为负");
        }
        if (personCount < 1) {
            throw new IllegalArgumentException("人数至少为 1");
        }
        if (status == null) {
            throw new IllegalArgumentException("票品状态不能为空");
        }
    }

}
