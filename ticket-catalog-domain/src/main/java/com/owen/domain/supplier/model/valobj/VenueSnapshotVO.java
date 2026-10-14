package com.owen.domain.supplier.model.valobj;

/**
 * 供应商场馆快照。只用于申请平台 POI，不单独落库。
 */
public record VenueSnapshotVO(String name, String address) {

    public VenueSnapshotVO {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("场馆名称不能为空");
        }
    }

    public static VenueSnapshotVO of(String name, String address) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return new VenueSnapshotVO(name, address);
    }

}
