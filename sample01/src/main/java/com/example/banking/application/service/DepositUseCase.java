package com.example.banking.application.service;

import com.example.banking.domain.account.AccountId;
import com.example.banking.domain.account.Money;

public interface DepositUseCase {

    /**
     * 指定された口座に入金します。
     * @param id 入金対象の口座ID
     * @param amount 入金額
     */
    void deposit(AccountId id, Money amount);
} 