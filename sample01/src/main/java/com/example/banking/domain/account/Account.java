package com.example.banking.domain.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 口座アグリゲート。
 * 口座に関する不変条件とビジネスロジック（入金、出金）をカプセル化します。
 * アグリゲートのルートエンティティとして、このオブジェクトを介してのみ口座の状態が変更されます。
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

    /** 口座ID。アグリゲートの不変の識別子。 */
    private final AccountId id;
    /** 口座番号。ビジネス上の一意な識別子。 */
    private final AccountNumber accountNumber;
    /** 顧客名。 */
    private final CustomerName customerName;
    /** 残高。 */
    private Money balance;
    /** 楽観的ロック用のバージョン。 */
    private long version;

    /**
     * 新しい口座を開設するためのファクトリメソッド。
     * 初期残高はゼロで作成されます。
     * @param customerName 顧客名
     * @return 新しく作成されたAccountインスタンス
     */
    public static Account open(CustomerName customerName) {
        var id = AccountId.newId();
        var accountNumber = AccountNumber.generate();
        var initialBalance = new Money(BigDecimal.ZERO, Money.JPY);
        return new Account(id, accountNumber, customerName, initialBalance, 0L);
    }

    /**
     * 永続化層からアカウントを再構成するためのファクトリメソッド。
     * @param id 口座ID
     * @param accountNumber 口座番号
     * @param customerName 顧客名
     * @param balance 残高
     * @param version バージョン
     * @return 再構成されたAccountインスタンス
     */
    public static Account reconstitute(AccountId id, AccountNumber accountNumber, CustomerName customerName, Money balance, long version) {
        return new Account(id, accountNumber, customerName, balance, version);
    }

    /**
     * 口座に入金します。
     * @param amount 入金する金額
     */
    public void deposit(Money amount) {
        // ビジネスルール：入金額は正の値でなければならない
        if (amount.isLessThanOrEqualTo(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * 口座から出金します。
     * @param amount 出金する金額
     * @throws InsufficientBalanceException 残高が不足している場合
     */
    public void withdraw(Money amount) {
        // ビジネスルール：出金額は正の値でなければならない
        if (amount.isLessThanOrEqualTo(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        // ビジネスルール：出金額は残高を超えてはならない
        if (this.balance.isLessThan(amount)) {
            throw new InsufficientBalanceException("Insufficient balance.");
        }
        this.balance = this.balance.subtract(amount);
    }
} 