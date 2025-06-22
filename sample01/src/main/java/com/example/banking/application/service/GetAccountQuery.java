package com.example.banking.application.service;

import com.example.banking.domain.account.Account;
import com.example.banking.domain.account.AccountId;

import java.util.Optional;

/**
 * 口座情報取得クエリのインターフェース。
 * CQRSのクエリ側に相当します。
 */
public interface GetAccountQuery {

    /**
     * 口座IDで口座情報を取得します。
     * @param id 取得対象の口座ID
     * @return 口座情報。見つからない場合はOptional.empty()
     */
    Optional<Account> getAccount(AccountId id);
} 