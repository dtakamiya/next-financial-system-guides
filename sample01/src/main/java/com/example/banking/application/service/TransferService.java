package com.example.banking.application.service;

import com.example.banking.application.saga.TransferRequestedEvent;
import com.example.banking.domain.account.AccountId;
import com.example.banking.domain.account.Money;
import com.example.banking.domain.transfer.Transfer;
import com.example.banking.domain.transfer.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 振込に関するユースケースを実装するアプリケーションサービス。
 */
@Service
@RequiredArgsConstructor
public class TransferService implements RequestTransferUseCase {

    /**
     * 振込リポジトリ。ドメイン層のインターフェースに依存します。
     */
    private final TransferRepository transferRepository;
    /**
     * ドメインイベントを発行するためのパブリッシャー。
     * SpringのApplicationEventPublisherを利用して、Sagaのトリガーとなるイベントを発行します。
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 振込依頼ユースケース。
     * このメソッドの責務は、振込依頼をシステムに受け付け、
     * それを永続化し、非同期処理のトリガーとなるイベントを発行することです。
     * 実際の振込処理（出金・入金）はSagaが担当します。
     *
     * @param sourceAccountId 振込元口座ID
     * @param destinationAccountId 振込先口座ID
     * @param money 振込金額
     * @return 永続化されたTransferアグリゲート
     */
    @Override
    @Transactional
    public Transfer requestTransfer(AccountId sourceAccountId, AccountId destinationAccountId, Money money) {
        // 口座の存在チェックなどは、より堅牢にするならここで行うか、Sagaの最初のステップで行う。
        // ここではシンプルにするためSagaに委ねる。

        // 1. Transferアグリゲートを生成し、REQUESTED状態で永続化する
        Transfer transfer = Transfer.request(sourceAccountId, destinationAccountId, money);
        transferRepository.save(transfer);

        // 2. Sagaを起動するためのドメインイベントを発行する
        eventPublisher.publishEvent(new TransferRequestedEvent(this, transfer.getId()));

        return transfer;
    }
} 