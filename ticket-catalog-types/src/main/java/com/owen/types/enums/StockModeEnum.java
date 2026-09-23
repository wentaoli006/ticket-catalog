package com.owen.types.enums;

import lombok.Getter;

/**
 * 合作库存模式。1 共享库存（供应商兜底），2 买断库存。
 */
@Getter
public enum StockModeEnum {

    SHARED(1),
    BUYOUT(2),
    ;

    private final int code;

    StockModeEnum(int code) {
        this.code = code;
    }

    public static StockModeEnum of(int code) {
        for (StockModeEnum mode : values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        throw new IllegalArgumentException("未知库存模式: " + code);
    }

}
