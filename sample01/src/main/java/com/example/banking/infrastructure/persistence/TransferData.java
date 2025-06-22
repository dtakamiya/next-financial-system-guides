package com.example.banking.infrastructure.persistence;

import com.example.banking.domain.transfer.Transfer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Transferアグリゲートを永続化するためのデータ転送オブジェクト(DTO)。
 * このクラスの構造は、データベースの`transfers`テーブルのスキーマに対応しています。
 *
 * @see AccountData
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferData {
    /** 振込ID (UUID) */
    private UUID id;
    /** 送金元口座ID (UUID) */
    private UUID sourceAccountId;
    /** 送金先口座ID (UUID) */
    private UUID destinationAccountId;
    /** 金額 */
    private BigDecimal amount;
    /** 通貨 */
    private String currency;
    /** 振込ステータス (REQUESTED, COMPLETED, FAILED) */
    private String status;
    /** 楽観的ロック用のバージョン番号 */
    private long version;
} 