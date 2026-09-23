package com.owen.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 供应商，对应 supplier。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Supplier {

    /** 自增ID；供应商ID */
    private Long id;
    /** 供应商名称 */
    private String name;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

}
