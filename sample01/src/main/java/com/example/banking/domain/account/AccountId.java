package com.example.banking.domain.account;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * 口座IDを表す値オブジェクト。
 * UUIDをラップし、口座IDというドメインの概念を明確にします。
 * これにより、単なるUUIDではなく、型レベルで他のIDと区別することができます。
 */
public record AccountId(UUID value) implements Serializable {
    public AccountId {
        Objects.requireNonNull(value, "AccountId cannot be null");
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }
} 