package com.owen.infrastructure.adapter.repository;

import com.owen.domain.supplier.model.entity.Poi;
import com.owen.domain.supplier.repository.IPoiRepository;
import com.owen.infrastructure.adapter.repository.converter.PoiConverter;
import com.owen.infrastructure.dao.IPoiDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PoiRepository implements IPoiRepository {

    private final IPoiDao poiDao;
    private final PoiConverter poiConverter;

    public PoiRepository(IPoiDao poiDao, PoiConverter poiConverter) {
        this.poiDao = poiDao;
        this.poiConverter = poiConverter;
    }

    @Override
    public Optional<Poi> findByPoiId(Long poiId) {
        return Optional.ofNullable(poiConverter.toEntity(poiDao.selectByPoiId(poiId)));
    }

    @Override
    public Optional<Poi> findByVenue(Long supplierId, String venueName) {
        return Optional.ofNullable(poiConverter.toEntity(poiDao.selectBySupplierAndName(supplierId, venueName)));
    }

    @Override
    public void save(Poi poi) {
        com.owen.infrastructure.dao.po.Poi po = poiConverter.toPo(poi);
        if (po.getId() == null) {
            poiDao.insert(po);
        } else {
            poiDao.update(po);
        }
    }

}
