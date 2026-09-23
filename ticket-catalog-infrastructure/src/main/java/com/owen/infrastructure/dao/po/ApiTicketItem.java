package com.owen.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商原始票品，对应 api_ticket_item。展示库存存在 stockShowNum，供应商兜底。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiTicketItem {

    /** 自增ID */
    private Long id;
    /** 供应商ID */
    private Long supplierId;
    /** 供应商侧票品ID */
    private String supplierTicketItemId;
    /** 场次ID；关联 api_session.id */
    private Long sessionId;
    /** 票品名称 */
    private String name;
    /** 票品描述 */
    private String description;
    /** 最小售卖数量 */
    private Integer minSaleQty;
    /** 最大售卖数量 */
    private Integer maxSaleQty;
    /** 价格 */
    private BigDecimal price;
    /** 币种 */
    private String currency;
    /** 展示库存；供应商兜底，本系统不扣减 */
    private Integer stockShowNum;
    /** 供应商 PLU 编码 */
    private String plu;
    /** 聚合后的 PLU ID；关联 api_plu.id，未聚合为空 */
    private Long pluId;
    /** 票品状态；0停售 1可售 */
    private Integer status;
    /** 人数；套票时大于 1 */
    private Integer personCount;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

}
