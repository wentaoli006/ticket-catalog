package com.owen.domain.supplier.acl;

/**
 * 平台 POI 申请端口。按供应商名称 + 供应商项目 ID 申请，返回平台 poiId。
 */
public interface PoiPort {

    Long apply(String supplierName, String supplierProjectId);

}
