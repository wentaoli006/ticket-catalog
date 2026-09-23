package com.owen.infrastructure.dao;

import com.owen.infrastructure.dao.po.Poi;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IPoiDao {

    int insert(Poi poi);

    int update(Poi poi);

    Poi selectById(Long id);

    Poi selectByPoiId(Long poiId);

    Poi selectBySupplierAndName(Long supplierId, String supplierPoiName);

    List<Poi> selectBySupplierId(Long supplierId);

}
