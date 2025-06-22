package com.example.banking.domain.account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * 金額を表す値オブジェクト (Value Object)。
 * 値オブジェクトは、その属性によって識別される不変のオブジェクトです。
 * - 不変 (Immutable): 一度作成されたら状態は変更されない。
 * - 等価性: すべての属性が同じであれば、同じオブジェクトと見なされる。
 * - 自己検証: オブジェクトは常に有効な状態であることが保証される。
 *
 * このクラスは「金額」というドメインの概念をコードで表現し、
 * 金額に関するロジック（計算、比較など）をカプセル化します。
 *
 * @param amount 金額
 * @param currency 通貨
 */
public record Money(BigDecimal amount, Currency currency) implements Serializable {

    public static final Currency JPY = Currency.getInstance("JPY");

    public Money {
        // 不変条件: 金額と通貨はnullであってはならない。
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, JPY);
    }

    /**
     * 文字列からJPY通貨のMoneyインスタンスを生成するファクトリメソッド。
     * このメソッドを使用することで、金額の生成方法を一元管理できます。
     *
     * @param amount 文字列形式の金額
     * @return 新しいMoneyインスタンス
     */
    public static Money of(String amount) {
        return new Money(new BigDecimal(amount), JPY);
    }

    /**
     * 金額を加算します。
     * このオブジェクトは不変であるため、新しいMoneyインスタンスを返します。
     *
     * @param other 加算する金額
     * @return 加算後の新しいMoneyインスタンス
     * @throws IllegalArgumentException 通貨が異なる場合
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * 金額を減算します。
     *
     * @param other 減算する金額
     * @return 減算後の新しいMoneyインスタンス
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * この金額が引数の金額より小さいかどうかを判定します。
     *
     * @param other 比較対象の金額
     * @return 小さい場合はtrue
     */
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * 金額が負の値かどうかを判定します。
     *
     * @return 負の値の場合はtrue
     */
    public boolean isNegative() {
        return this.amount.signum() < 0;
    }

    /**
     * 金額が負の値またはゼロかどうかを判定します。
     *
     * @return 負の値またはゼロの場合はtrue
     */
    public boolean isNegativeOrZero() {
        return this.amount.signum() <= 0;
    }
} 