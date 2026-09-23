package com.owen.domain.supplier.model.entity;

import com.owen.domain.supplier.model.valobj.VenueSnapshot;

import java.math.BigDecimal;

/**
 * 平台 POI。poiId 由外部 RPC 分配，不是本表主键。
 */
public class Poi {

    private Long id;
    private final Long supplierId;
    private final String supplierPoiName;
    private final String supplierPoiAddress;
    private final Long poiId;
    private final String poiName;
    private final BigDecimal longitude;
    private final BigDecimal latitude;

    private Poi(Long id, Long supplierId, String supplierPoiName, String supplierPoiAddress,
                 Long poiId, String poiName, BigDecimal longitude, BigDecimal latitude) {
        this.id = id;
        this.supplierId = supplierId;
        this.supplierPoiName = supplierPoiName;
        this.supplierPoiAddress = supplierPoiAddress;
        this.poiId = poiId;
        this.poiName = poiName;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * 新项目申请到平台 poiId 后创建。经纬度供应商静态接口不给，先记 0，后续人工校准。
     */
    public static Poi applied(Long supplierId, VenueSnapshot venue, Long poiId) {
        if (poiId == null || poiId <= 0) {
            throw new IllegalArgumentException("POI ID 不合法");
        }
        return new Poi(null, supplierId, venue.name(), venue.address(), poiId, venue.name(),
                BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static Poi restore(Long id, Long supplierId, String supplierPoiName, String supplierPoiAddress,
                               Long poiId, String poiName, BigDecimal longitude, BigDecimal latitude) {
        return new Poi(id, supplierId, supplierPoiName, supplierPoiAddress, poiId, poiName, longitude, latitude);
    }

    public Long getId() {
        return id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getSupplierPoiName() {
        return supplierPoiName;
    }

    public String getSupplierPoiAddress() {
        return supplierPoiAddress;
    }

    public Long getPoiId() {
        return poiId;
    }

    public String getPoiName() {
        return poiName;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

}
