package com.example.banking.presentation.rest;

import jakarta.validation.constraints.NotBlank;

/**
 * 口座開設APIへのリクエストボディを表すDTO。
 * Java 17のrecord機能を利用して不変なデータクラスを簡潔に定義しています。
 */
public record OpenAccountRequest(
    /** 顧客名 */
    @NotBlank(message = "Customer name must not be blank.")
    String customerName
) {} 