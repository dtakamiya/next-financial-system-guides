package com.example.banking.presentation.rest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 振込APIへのリクエストボディを表すDTO。
 * Java 17のrecord機能を利用して不変なデータクラスを簡潔に定義しています。
 */
public record TransferRequest(
    /** 送金元口座ID */
    @NotNull
    UUID sourceAccountId,
    /** 送金先口座ID */
    @NotNull
    UUID destinationAccountId,
    /** 振込額 */
    @NotNull
    @Positive(message = "Amount must be positive.")
    BigDecimal amount
) {} 