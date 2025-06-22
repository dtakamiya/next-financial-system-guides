package com.example.banking.presentation.rest;

import com.example.banking.application.service.DepositUseCase;
import com.example.banking.application.service.GetAccountQuery;
import com.example.banking.application.service.OpenAccountUseCase;
import com.example.banking.domain.account.Account;
import com.example.banking.domain.account.AccountId;
import com.example.banking.domain.account.CustomerName;
import com.example.banking.domain.account.Money;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * 口座管理機能に関するREST APIエンドポイントを提供するコントローラ。
 * この層の責務は、HTTPリクエストを受け取り、適切なアプリケーションサービス（ユースケース）を呼び出し、
 * 結果をHTTPレスポンスとしてクライアントに返すことです。
 * ドメインの知識は持ちません。
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    /** 口座開設ユースケース */
    private final OpenAccountUseCase openAccountUseCase;
    /** 入金ユースケース */
    private final DepositUseCase depositUseCase;
    /** 口座情報取得クエリ */
    private final GetAccountQuery getAccountQuery;

    /**
     * 口座開設API (POST /api/accounts)
     * @param request 口座開設リクエストのボディ
     * @return 作成されたリソースの場所を示すヘッダと、リソースの詳細を含むレスポンス (HTTP 201 Created)
     */
    @PostMapping
    public ResponseEntity<AccountDetailsResponse> openAccount(@Valid @RequestBody OpenAccountRequest request) {
        Account account = openAccountUseCase.openAccount(
                new CustomerName(request.getCustomerName()),
                Money.of(request.getInitialDeposit().toPlainString())
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(account.getId().value())
                .toUri();

        return ResponseEntity.created(location).body(AccountDetailsResponse.from(account));
    }

    /**
     * 口座情報取得API (GET /api/accounts/{accountId})
     * @param accountId 取得対象の口座ID
     * @return 口座詳細情報 (HTTP 200 OK) または Not Found (HTTP 404)
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDetailsResponse> getAccount(@PathVariable UUID accountId) {
        return getAccountQuery.getAccountDetails(new AccountId(accountId))
                .map(AccountDetailsResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 入金API (POST /api/accounts/{accountId}/deposits)
     * @param accountId 入金対象の口座ID
     * @param request 入金リクエストのボディ
     * @return 成功を示すレスポンス (HTTP 200 OK)
     */
    @PostMapping("/{accountId}/deposits")
    public ResponseEntity<Void> deposit(@PathVariable UUID accountId, @Valid @RequestBody DepositRequest request) {
        depositUseCase.deposit(
                new AccountId(accountId),
                Money.of(request.getAmount().toPlainString())
        );
        return ResponseEntity.ok().build();
    }
} 