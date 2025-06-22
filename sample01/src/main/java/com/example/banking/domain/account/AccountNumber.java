package com.example.banking.domain.account;

import java.io.Serializable;
import java.util.Objects;

/**
 * 口座番号を表す値オブジェクト。
 * 文字列をラップし、口座番号というドメインの概念を明確にします。
 * 将来的に口座番号に関する検証ロジック（例: チェックデジット）が追加された場合、
 * このクラスにそのロジックをカプセル化できます。
 */
public record AccountNumber(String value) implements Serializable {
    public AccountNumber {
        Objects.requireNonNull(value, "AccountNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AccountNumber cannot be blank");
        }
    }
} 