package com.example.banking.domain.account;

import java.util.Optional;

/**
 * 口座アグリゲートのためのリポジトリインターフェース。
 *
 * リポジトリは、ドメインオブジェクトの永続化を抽象化する責務を持ちます。
 * ドメイン層は、このインターフェースにのみ依存し、具体的な永続化技術（DB, ORMなど）からは隔離されます。
 * 実装はインフラストラクチャ層が担当します（依存性の逆転）。
 *
 * このインターフェースは、ドメインエキスパートが理解できる言葉で定義されるべきです（例: 「口座を探す」「口座を保存する」）。
 */
public interface AccountRepository {

    /**
     * IDで口座アグリゲートを検索します。
     *
     * @param id 検索する口座のID
     * @return 見つかった場合はOptionalでラップされたAccount、見つからない場合は空のOptional
     */
    Optional<Account> findById(AccountId id);

    /**
     * 口座アグリゲートを保存（新規作成または更新）します。
     *
     * @param account 保存する口座アグリゲート
     */
    void save(Account account);

    /**
     * 次の利用可能な口座番号を採番します。
     *
     * @return 新しい口座番号
     */
    AccountNumber nextAccountNumber();

    /**
     * 口座を更新します。
     * @param account 更新する口座アグリゲート
     */
    void update(Account account);

} 