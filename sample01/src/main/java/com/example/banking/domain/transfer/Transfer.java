package com.example.banking.domain.transfer;

import com.example.banking.domain.account.AccountId;
import com.example.banking.domain.account.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 振込アグリゲート。
 * 振込という一連のプロセス全体の状態と不変条件を管理します。
 * このアグリゲートはSagaによって操作され、振込の状態（依頼中、完了、失敗）を遷移させます。
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Transfer {

    /** 振込ID。アグリゲートの不変の識別子。 */
    private final TransferId id;
    /** 送金元口座ID。 */
    private final AccountId sourceAccountId;
    /** 送金先口座ID。 */
    private final AccountId destinationAccountId;
    /** 振込金額。 */
    private final Money money;
    /** 振込ステータス。 */
    private TransferStatus status;
    /** 楽観的ロック用のバージョン。 */
    private long version;

    /**
     * 振込の状態を定義する列挙型。
     */
    public enum TransferStatus {
        /** 振込依頼中 */
        REQUESTED,
        /** 振込完了 */
        COMPLETED,
        /** 振込失敗 */
        FAILED
    }

    /**
     * 新しい振込をリクエストするためのファクトリメソッド。
     * 振込はまず`REQUESTED`状態で作成されます。
     * @param sourceAccountId 送金元口座ID
     * @param destinationAccountId 送金先口座ID
     * @param money 振込金額
     * @return 新しく作成されたTransferインスタンス
     */
    public static Transfer request(AccountId sourceAccountId, AccountId destinationAccountId, Money money) {
        // ビジネスルール: 自分自身への振込はできない
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same.");
        }
        var id = TransferId.newId();
        return new Transfer(id, sourceAccountId, destinationAccountId, money, TransferStatus.REQUESTED, 0L);
    }

    /**
     * 永続化層から振込を再構成するためのファクトリメソッド。
     * @param id 振込ID
     * @param sourceAccountId 送金元口座ID
     * @param destinationAccountId 送金先口座ID
     * @param money 振込金額
     * @param status 振込ステータス
     * @param version バージョン
     * @return 再構成されたTransferインスタンス
     */
    public static Transfer reconstitute(TransferId id, AccountId sourceAccountId, AccountId destinationAccountId, Money money, TransferStatus status, long version) {
        return new Transfer(id, sourceAccountId, destinationAccountId, money, status, version);
    }

    /**
     * 振込を完了状態にします。
     * このメソッドはSagaから呼び出されることを想定しています。
     */
    public void complete() {
        // ステータスの遷移ルール: REQUESTED状態からのみCOMPLETEDに遷移できる
        if (this.status != TransferStatus.REQUESTED) {
            throw new IllegalStateException("Transfer is not in a state that can be completed.");
        }
        this.status = TransferStatus.COMPLETED;
    }

    /**
     * 振込を失敗状態にします。
     * このメソッドはSagaから呼び出されることを想定しています。
     */
    public void fail() {
        // ステータスの遷移ルール: REQUESTED状態からのみFAILEDに遷移できる
        if (this.status != TransferStatus.REQUESTED) {
            // 冪等性を考慮し、既に失敗している場合は何もしない
            if (this.status == TransferStatus.FAILED) {
                return;
            }
            throw new IllegalStateException("Transfer is not in a state that can be failed.");
        }
        this.status = TransferStatus.FAILED;
    }
} 