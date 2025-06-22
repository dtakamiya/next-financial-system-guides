package com.example.banking.infrastructure.persistence;

import com.example.banking.domain.account.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.dao.OptimisticLockFailureException;

import java.util.Currency;
import java.util.Optional;

/**
 * AccountRepositoryのMyBatisによる実装。
 * このクラスはインフラストラクチャ層に属し、ドメイン層で定義されたインターフェースを実装します。
 * ドメインオブジェクト(Account)と永続化データオブジェクト(AccountData)の間の変換（マッピング）を担当します。
 * これにより、ドメイン層は永続化の具体的な技術（ここではMyBatisやRDB）から完全に独立します。
 */
@Repository
@RequiredArgsConstructor
public class MyBatisAccountRepository implements AccountRepository {

    private final AccountMapper accountMapper;

    @Override
    public Optional<Account> findById(AccountId id) {
        return accountMapper.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public void save(Account account) {
        AccountData data = toData(account);

        // versionがnullの場合は新規作成、それ以外は更新
        if (account.getVersion() == null) {
            accountMapper.insert(data);
        } else {
            int updatedRows = accountMapper.update(data);
            // 楽観的ロック: 更新された行数が0の場合、他で更新されたと判断し例外をスロー
            if (updatedRows == 0) {
                throw new OptimisticLockFailureException("Account has been updated by another transaction: " + account.getId().value());
            }
        }
    }

    @Override
    public AccountNumber nextAccountNumber() {
        // 本来はDBのシーケンスなどから採番すべきだが、ここでは簡略化
        return new AccountNumber(String.format("%010d", System.currentTimeMillis()));
    }

    /**
     * 永続化データオブジェクト(AccountData)をドメインオブジェクト(Account)に変換します。
     * @param data データベースから取得したデータ
     * @return Accountドメインオブジェクト
     */
    private Account toDomain(AccountData data) {
        return new Account(
                new AccountId(data.getId()),
                new AccountNumber(data.getAccountNumber()),
                new CustomerName(data.getCustomerName()),
                new Money(data.getBalance().toPlainString()),
                data.getVersion()
        );
    }

    /**
     * ドメインオブジェクト(Account)を永続化データオブジェクト(AccountData)に変換します。
     * @param domain Accountドメインオブジェクト
     * @return データベースに保存するためのデータ
     */
    private AccountData toData(Account domain) {
        return new AccountData(
                domain.getId().value(),
                domain.getAccountNumber().value(),
                domain.getCustomerName().value(),
                domain.getBalance().amount(),
                domain.getVersion()
        );
    }
} 