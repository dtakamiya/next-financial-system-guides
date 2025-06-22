package com.example.banking.domain.account;

/**
 * 残高不足を表すドメイン固有の例外。
 * 特定のビジネスルール違反（ここでは残高不足）を明確に示すために、
 * 汎用的な例外(e.g., IllegalStateException)ではなく、カスタム例外を定義します。
 * これにより、アプリケーション層でこの例外をキャッチし、
 * ビジネス要件に応じたエラーハンドリング（例: ユーザーへの分かりやすいメッセージ表示）が可能になります。
 */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
} 