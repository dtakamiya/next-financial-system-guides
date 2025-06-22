package com.example.banking.presentation.rest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 入金APIへのリクエストボディを表すDTO。
 * Java 17のrecord機能を利用して不変なデータクラスを簡潔に定義しています。
 */
public record DepositRequest(
    /** 入金額 */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive.")
    BigDecimal amount
) {} 