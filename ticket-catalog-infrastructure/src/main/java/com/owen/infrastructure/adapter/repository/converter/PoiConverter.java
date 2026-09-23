package com.owen.infrastructure.adapter.repository.converter;

import com.owen.domain.supplier.model.entity.Poi;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PoiConverter {

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    com.owen.infrastructure.dao.po.Poi toPo(Poi poi);

    default Poi toEntity(com.owen.infrastructure.dao.po.Poi po) {
        if (po == null) {
            return null;
        }
        return Poi.restore(
                po.getId(),
                po.getSupplierId(),
                po.getSupplierPoiName(),
                po.getSupplierPoiAddress(),
                po.getPoiId(),
                po.getPoiName(),
                po.getLongitude(),
                po.getLatitude()
        );
    }

}
