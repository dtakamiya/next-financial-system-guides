package com.example.banking.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Accountアグリゲートを永続化するためのデータ転送オブジェクト(DTO)。
 * このクラスの構造は、データベースの`accounts`テーブルのスキーマに対応しています。
 * ドメインモデル(Account)と永続化モデル(このクラス)を分離することで、
 * それぞれの関心事（ビジネスルール vs 永続化技術）に集中できます。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountData {
    private UUID id;
    private String accountNumber;
    private String customerName;
    private BigDecimal balance;
    private Long version;
} 