package com.example.banking.domain.transfer;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * UUIDをラップすることで、振込IDというドメイン固有の型を定義します。
 */
public record TransferId(UUID value) implements Serializable {
    public TransferId {
        // 不変条件: TransferIdはnullであってはならない。
        Objects.requireNonNull(value, "TransferId cannot be null");
    }

    /**
     * 新しい振込IDを生成するファクトリメソッド。
     * @return 新しいTransferIdインスタンス
     */
    public static TransferId newId() {
        return new TransferId(UUID.randomUUID());
    }
} 