package com.owen.types.enums;

import lombok.Getter;

/**
 * 可售状态。项目、场次、票品共用：0 停售，1 可售。
 */
@Getter
public enum SaleStatusEnum {

    OFF_SALE(0),
    ON_SALE(1),
    ;

    private final int code;

    SaleStatusEnum(int code) {
        this.code = code;
    }

    public static SaleStatusEnum of(int code) {
        for (SaleStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知售卖状态: " + code);
    }

}
