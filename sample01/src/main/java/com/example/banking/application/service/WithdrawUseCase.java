package com.example.banking.application.service;

import com.example.banking.domain.account.AccountId;
import com.example.banking.domain.account.Money;

/**
 * 出金ユースケースのインターフェース。
 */
public interface WithdrawUseCase {

    /**
     * 指定された口座から出金します。
     *
     * @param id 出金対象の口座ID
     * @param amount 出金額
     */
    void withdraw(AccountId id, Money amount);
} 