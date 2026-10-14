package com.owen.infrastructure.adapter.repository.converter;

import com.owen.domain.supplier.model.entity.ApiTicketItemEntity;
import com.owen.types.dp.Money;
import com.owen.types.enums.SaleStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = SaleStatusEnum.class)
public interface ApiTicketItemConverter {

    @Mapping(target = "price", expression = "java(item.getPrice().getAmount())")
    @Mapping(target = "currency", expression = "java(item.getPrice().getCurrency())")
    @Mapping(target = "status", expression = "java(item.getStatus().getCode())")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    com.owen.infrastructure.dao.po.ApiTicketItem toPo(ApiTicketItemEntity item);

    default ApiTicketItemEntity toEntity(com.owen.infrastructure.dao.po.ApiTicketItem po) {
        if (po == null) {
            return null;
        }
        return ApiTicketItemEntity.restore(
                po.getId(),
                po.getSupplierId(),
                po.getSupplierTicketItemId(),
                po.getSessionId(),
                po.getName(),
                po.getDescription(),
                po.getMinSaleQty(),
                po.getMaxSaleQty(),
                new Money(po.getPrice(), po.getCurrency()),
                po.getStockShowNum(),
                po.getPlu(),
                po.getPluId(),
                SaleStatusEnum.of(po.getStatus()),
                po.getPersonCount()
        );
    }

}
