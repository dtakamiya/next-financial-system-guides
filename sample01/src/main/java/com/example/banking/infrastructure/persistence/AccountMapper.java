package com.example.banking.infrastructure.persistence;

import com.example.banking.domain.account.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 口座データの永続化を担うMyBatis Mapperインターフェース。
 * このインターフェースのメソッドは、`resources`ディレクトリ以下の対応するXMLファイルにマッピングされたSQLを実行します。
 */
@Mapper
public interface AccountMapper {

    Optional<AccountData> findById(@Param("id") UUID id);

    void insert(AccountData accountData);

    int update(AccountData accountData);

    String nextAccountNumber();
} 