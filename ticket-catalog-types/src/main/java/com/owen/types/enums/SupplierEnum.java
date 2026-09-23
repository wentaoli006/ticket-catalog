package com.owen.types.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 供应商字典。id 与 supplier 表主键一致，name 是对外申请 POI 时使用的供应商名称。
 * 后续接入新供应商只在这里追加，不改已有取值。
 */
@Getter
public enum SupplierEnum {

    SHANGHAI_GRAND_THEATRE(1L, "上海大剧院"),
    ;

    private final Long id;
    private final String name;

    SupplierEnum(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static SupplierEnum of(Long id) {
        return Arrays.stream(values())
                .filter(supplier -> supplier.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知供应商ID: " + id));
    }

}
