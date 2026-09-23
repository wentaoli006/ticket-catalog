package com.owen.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 供应商原始项目，对应 api_project。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiProject {

    /** 自增ID */
    private Long id;
    /** 供应商ID */
    private Long supplierId;
    /** 供应商侧项目ID */
    private String supplierProjectId;
    /** 平台 POI ID；关联 poi.poi_id，不是 poi 表主键 */
    private Long poiId;
    /** 项目名称 */
    private String name;
    /** 海报地址 */
    private String posterUrl;
    /** 购票须知 */
    private String ticketNotice;
    /** 售卖状态；0停售 1在售 */
    private Integer saleStatus;
    /** 是否实名；0否 1是 */
    private Integer realName;
    /** 是否选座；0否 1是 */
    private Integer seatSelection;
    /** IANA 时区，如 Asia/Shanghai。场次与售卖时间都按此时区解释 */
    private String timezone;
    /** 最早场次开始时间；项目时区下的钟面时间 */
    private LocalDateTime earliestSessionStartTime;
    /** 最晚场次开始时间；项目时区下的钟面时间 */
    private LocalDateTime latestSessionStartTime;
    /** 合作方式；1共享库存 2买断库存 */
    private Integer stockMode;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

}
