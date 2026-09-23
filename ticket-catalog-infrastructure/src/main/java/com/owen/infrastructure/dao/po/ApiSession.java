package com.owen.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 供应商原始场次，对应 api_session。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiSession {

    /** 自增ID */
    private Long id;
    /** 供应商ID */
    private Long supplierId;
    /** 项目ID；关联 api_project.id */
    private Long projectId;
    /** 供应商侧场次ID */
    private String supplierSessionId;
    /** 场次名称 */
    private String name;
    /** 场次开始时间；项目时区下的钟面时间 */
    private LocalDateTime sessionStartTime;
    /** 场次结束时间；项目时区下的钟面时间 */
    private LocalDateTime sessionEndTime;
    /** 开售时间；项目时区下的钟面时间 */
    private LocalDateTime saleStartTime;
    /** 停售时间；项目时区下的钟面时间 */
    private LocalDateTime saleEndTime;
    /** 是否实名；0否 1是 */
    private Integer realName;
    /** 是否选座；0否 1是 */
    private Integer seatSelection;
    /** 场次状态；0停售 1可售 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

}
