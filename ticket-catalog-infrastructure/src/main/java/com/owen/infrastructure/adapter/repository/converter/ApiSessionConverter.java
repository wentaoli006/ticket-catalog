package com.owen.infrastructure.adapter.repository.converter;

import com.owen.domain.supplier.model.entity.ApiSessionEntity;
import com.owen.types.enums.SaleStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = SaleStatusEnum.class)
public interface ApiSessionConverter {

    @Mapping(target = "realName", expression = "java(session.isRealName() ? 1 : 0)")
    @Mapping(target = "seatSelection", expression = "java(session.isSeatSelection() ? 1 : 0)")
    @Mapping(target = "status", expression = "java(session.getStatus().getCode())")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    com.owen.infrastructure.dao.po.ApiSession toPo(ApiSessionEntity session);

    default ApiSessionEntity toEntity(com.owen.infrastructure.dao.po.ApiSession po) {
        if (po == null) {
            return null;
        }
        return ApiSessionEntity.restore(
                po.getId(),
                po.getSupplierId(),
                po.getProjectId(),
                po.getSupplierSessionId(),
                po.getName(),
                po.getSessionStartTime(),
                po.getSessionEndTime(),
                po.getSaleStartTime(),
                po.getSaleEndTime(),
                po.getRealName() != null && po.getRealName() == 1,
                po.getSeatSelection() != null && po.getSeatSelection() == 1,
                SaleStatusEnum.of(po.getStatus())
        );
    }

}
