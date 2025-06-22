package com.example.banking.application.service;

import com.example.banking.domain.account.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 口座管理に関するユースケースを実装するアプリケーションサービス。
 * アプリケーションサービスは、ドメインオブジェクト（アグリゲートやドメインサービス）を調整し、
 * 一つのビジネスユースケースを完結させる責務を持ちます。
 *
 * このクラスは、複数のユースケースインターフェース（`OpenAccountUseCase`, `DepositUseCase`など）を実装することで、
 * CQRS（コマンド・クエリ責務分離）の考え方を部分的に取り入れています。
 * - コマンド(状態変更): `openAccount`, `deposit`, `withdraw`
 * - クエリ(状態取得): `getAccountDetails`
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements OpenAccountUseCase, DepositUseCase, GetAccountQuery, WithdrawUseCase {

    /**
     * 口座リポジトリ。ドメイン層のインターフェースに依存することで、
     * インフラストラクチャ層の実装詳細から分離されています。
     */
    private final AccountRepository accountRepository;

    /**
     * 口座開設ユースケース。
     */
    @Override
    public Account openAccount(CustomerName customerName, Money initialDeposit) {
        AccountNumber accountNumber = accountRepository.nextAccountNumber();
        Account account = Account.open(accountNumber, customerName, initialDeposit);
        accountRepository.save(account);
        return account;
    }

    /**
     * 入金ユースケース。
     */
    @Override
    public void deposit(AccountId accountId, Money money) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId.value()));
        account.deposit(money);
        accountRepository.save(account);
    }

    /**
     * 出金ユースケース。
     */
    @Override
    public void withdraw(AccountId accountId, Money money) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId.value()));
        account.withdraw(money);
        accountRepository.save(account);
    }

    /**
     * 口座情報取得クエリ。
     * @Transactional(readOnly = true) を指定することで、この操作が状態を変更しない読み取り専用であることを示し、
     * 永続化層でのパフォーマンス最適化を期待できます。
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getAccountDetails(AccountId accountId) {
        return accountRepository.findById(accountId);
    }
} 