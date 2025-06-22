package com.example.banking.domain.account;

import java.io.Serializable;
import java.util.Objects;

/**
 * 顧客名を表す値オブジェクト。
 * 顧客名に関する制約（例: 文字数制限、使用可能文字）をこのクラスで一元管理できます。
 */
public record CustomerName(String value) implements Serializable {
    public CustomerName {
        Objects.requireNonNull(value, "CustomerName cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("CustomerName cannot be blank");
        }
    }
} 