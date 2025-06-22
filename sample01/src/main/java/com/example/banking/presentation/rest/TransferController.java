package com.example.banking.presentation.rest;

import com.example.banking.application.service.RequestTransferUseCase;
import com.example.banking.domain.account.AccountId;
import com.example.banking.domain.account.Money;
import com.example.banking.domain.transfer.Transfer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    /** 振込依頼ユースケース */
    private final RequestTransferUseCase requestTransferUseCase;

    /**
     * 振込依頼を受け付けるエンドポイント。
     *
     * @param request 振込リクエストのボディ
     * @return 処理の受付を示すレスポンス (HTTP 202 Accepted)
     */
    @PostMapping
    public ResponseEntity<Void> requestTransfer(@Valid @RequestBody TransferRequest request) {
        // アプリケーションサービスを呼び出して、振込依頼プロセスを開始する。
        // この呼び出しは同期的だが、内部で非同期のSagaをトリガーするイベントを発行する。
        Transfer transfer = requestTransferUseCase.requestTransfer(
                new AccountId(request.getSourceAccountId()),
                new AccountId(request.getDestinationAccountId()),
                Money.of(request.getAmount().toPlainString())
        );

        // 作成されたTransferリソースのURIを生成
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transfer.getId().value())
                .toUri();

        // HTTPステータスコード 202 Accepted を返す。
        // これは、リクエストは受け付けたが、処理が完了していないこと（非同期処理であること）を示す。
        // クライアントは、LocationヘッダのURIを使って、後で処理状況を確認できる（今回はそのAPIは未実装）。
        return ResponseEntity.accepted().location(location).build();
    }
} 