package com.owen.domain.supplier.model.valobj;

import com.owen.types.enums.SaleStatusEnum;

import java.time.ZoneId;

/**
 * 供应商原始项目。只承载标准化后的业务字段，不含供应商协议字段。
 */
public record RawProject(
        String supplierProjectId,
        String name,
        String posterUrl,
        String ticketNotice,
        SaleStatusEnum saleStatus,
        boolean realName,
        boolean seatSelection,
        ZoneId timezone,
        String venueName,
        String venueAddress
) {

    public RawProject {
        if (supplierProjectId == null || supplierProjectId.isBlank()) {
            throw new IllegalArgumentException("供应商项目ID不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("项目名称不能为空");
        }
        if (saleStatus == null) {
            throw new IllegalArgumentException("售卖状态不能为空");
        }
        if (timezone == null) {
            throw new IllegalArgumentException("时区不能为空");
        }
    }

}
