package com.example.banking.application.service;

import com.example.banking.domain.account.AccountId;
import com.example.banking.domain.account.Money;
import com.example.banking.domain.transfer.Transfer;

/**
 * 振込依頼ユースケースのインターフェース。
 */
public interface RequestTransferUseCase {

    /**
     * 振込を依頼します。
     * このメソッドはSagaプロセスのトリガーとなります。
     *
     * @param sourceAccountId 送金元口座ID
     * @param destinationAccountId 送金先口座ID
     * @param money 金額
     * @return 依頼された振込のID
     */
    Transfer requestTransfer(AccountId sourceAccountId, AccountId destinationAccountId, Money money);
} 