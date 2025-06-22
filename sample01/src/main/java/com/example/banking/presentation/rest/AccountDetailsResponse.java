package com.example.banking.presentation.rest;

import com.example.banking.domain.account.Account;
import java.math.BigDecimal;

/**
 * 口座詳細情報のレスポンスを表すDTO(Data Transfer Object)。
 * このクラスはイミュータブル（不変）であり、一度作成されたら状態は変更されません。
 * CQRSパターンにおけるクエリ側の戻り値として使用されます。
 * Java 17のrecord機能を利用して、ボイラープレートコードを削減しています。
 */
public record AccountDetailsResponse(
    /** 口座ID */
    String accountId,
    /** 口座番号 */
    String accountNumber,
    /** 顧客名 */
    String customerName,
    /** 残高 */
    BigDecimal balance
) {
    public static AccountDetailsResponse from(Account account) {
        return new AccountDetailsResponse(
                account.getId().value().toString(),
                account.getAccountNumber().value(),
                account.getCustomerName().value(),
                account.getBalance().amount()
        );
    }
} 