package com.owen.domain.supplier.repository;

import com.owen.domain.supplier.model.entity.Poi;

import java.util.Optional;

public interface IPoiRepository {

    Optional<Poi> findByPoiId(Long poiId);

    Optional<Poi> findByVenue(Long supplierId, String venueName);

    void save(Poi poi);

}
