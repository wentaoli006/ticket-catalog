package com.owen.domain.supplier.repository;

import com.owen.domain.supplier.model.entity.PoiEntity;

import java.util.Optional;

public interface IPoiRepository {

    Optional<PoiEntity> findByPoiId(Long poiId);

    Optional<PoiEntity> findByVenue(Long supplierId, String venueName);

    void save(PoiEntity poi);

}
