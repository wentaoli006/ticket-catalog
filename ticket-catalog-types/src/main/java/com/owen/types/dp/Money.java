package com.owen.types.dp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金额。禁止裸 BigDecimal 传递价格，币种必须显式带上。
 */
public final class Money {

    private static final int SCALE = 2;

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("币种不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为负");
        }
        this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public static Money cny(BigDecimal amount) {
        return new Money(amount, "CNY");
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

}
