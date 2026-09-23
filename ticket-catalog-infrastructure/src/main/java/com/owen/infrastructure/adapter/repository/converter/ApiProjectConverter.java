package com.owen.infrastructure.adapter.repository.converter;

import com.owen.domain.supplier.model.aggregate.ApiProjectAggregate;
import com.owen.infrastructure.dao.po.ApiProject;
import com.owen.types.enums.SaleStatusEnum;
import com.owen.types.enums.StockModeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.ZoneId;

@Mapper(componentModel = "spring", imports = {SaleStatusEnum.class, StockModeEnum.class, ZoneId.class})
public interface ApiProjectConverter {

    @Mapping(target = "saleStatus", expression = "java(project.getSaleStatus().getCode())")
    @Mapping(target = "realName", expression = "java(project.isRealName() ? 1 : 0)")
    @Mapping(target = "seatSelection", expression = "java(project.isSeatSelection() ? 1 : 0)")
    @Mapping(target = "timezone", expression = "java(project.getTimezone().getId())")
    @Mapping(target = "stockMode", expression = "java(project.getStockMode().getCode())")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    ApiProject toPo(ApiProjectAggregate project);

    default ApiProjectAggregate toAggregate(ApiProject po) {
        if (po == null) {
            return null;
        }
        return ApiProjectAggregate.restore(
                po.getId(),
                po.getSupplierId(),
                po.getSupplierProjectId(),
                po.getPoiId(),
                po.getName(),
                po.getPosterUrl(),
                po.getTicketNotice(),
                SaleStatusEnum.of(po.getSaleStatus()),
                po.getRealName() != null && po.getRealName() == 1,
                po.getSeatSelection() != null && po.getSeatSelection() == 1,
                ZoneId.of(po.getTimezone()),
                po.getEarliestSessionStartTime(),
                po.getLatestSessionStartTime(),
                StockModeEnum.of(po.getStockMode())
        );
    }

}
