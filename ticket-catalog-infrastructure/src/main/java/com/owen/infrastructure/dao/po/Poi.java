package com.owen.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 平台 POI，对应 poi。poiId 由外部 RPC 分配，不是本表主键 id。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Poi {

    /** 自增ID；仅本表主键 */
    private Long id;
    /** 供应商ID */
    private Long supplierId;
    /** 供应商侧 POI 名称 */
    private String supplierPoiName;
    /** 供应商侧 POI 地址 */
    private String supplierPoiAddress;
    /** 平台分配的 POI ID；外部 RPC 生成 */
    private Long poiId;
    /** 平台 POI 名称 */
    private String poiName;
    /** 经度 */
    private BigDecimal longitude;
    /** 纬度 */
    private BigDecimal latitude;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

}
