package com.example.banking.application.service;

import com.example.banking.domain.account.Account;
import com.example.banking.domain.account.CustomerName;
import com.example.banking.domain.account.Money;

/**
 * 口座開設ユースケースのインターフェース。
 */
public interface OpenAccountUseCase {

    /**
     * 新しい口座を開設します。
     * @param customerName 顧客名
     * @return 開設された口座のアグリゲート
     */
    Account openAccount(CustomerName customerName);
} 