package com.example.banking.application.saga;

import com.example.banking.application.service.DepositUseCase;
import com.example.banking.application.service.WithdrawUseCase;
import com.example.banking.domain.transfer.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 振込処理をオーケストレーションするSaga。
 * Sagaパターンは、複数のサービスやコンテキストにまたがる長期的なトランザクションを管理するためのデザインパターンです。
 * このクラスは、ドメインイベントをトリガーとして、一連のローカルトランザクション（出金、入金）を実行します。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransferSaga {

    private final TransferRepository transferRepository;
    private final WithdrawUseCase withdrawUseCase;
    private final DepositUseCase depositUseCase;

    /**
     * 振込依頼イベントをリッスンし、Sagaプロセスを開始します。
     *
     * @Async により、この処理は非同期で実行され、APIリクエストのスレッドをブロックしません。
     * @EventListener Springのイベントメカニズムにより、TransferRequestedEventが発行されるとこのメソッドが呼び出されます。
     * @Transactional このメソッド全体が単一のトランザクションとして実行されますが、
     *                内部で呼び出すユースケース（withdraw, deposit）はそれぞれ独自のトランザクションを持つため、
     *                このアノテーションはSagaの状態を読み書きする際の整合性を保証する目的で使われます。
     * @param event 振込依頼イベント
     */
    @Async
    @EventListener
    @Transactional
    public void handleTransferRequested(TransferRequestedEvent event) {
        log.info("Starting transfer saga for transferId: {}", event.getTransferId().value());

        // Sagaの状態を表すTransferアグリゲートを取得
        var transfer = transferRepository.findById(event.getTransferId())
                .orElseThrow(() -> new IllegalStateException("Transfer not found: " + event.getTransferId().value()));

        try {
            // ステップ1: 振込元口座から出金（ローカルトランザクション）
            withdrawUseCase.withdraw(transfer.getSourceAccountId(), transfer.getMoney());
            log.info("Withdraw successful for transferId: {}", transfer.getId().value());

            try {
                // ステップ2: 振込先口座へ入金（ローカルトランザクション）
                depositUseCase.deposit(transfer.getDestinationAccountId(), transfer.getMoney());
                log.info("Deposit successful for transferId: {}", transfer.getId().value());

                // ステップ3: Sagaの完了
                // すべてのステップが成功したので、Transferの状態をCOMPLETEDにする
                transfer.complete();
                transferRepository.save(transfer);
                log.info("Transfer saga completed for transferId: {}", transfer.getId().value());

            } catch (Exception e) {
                log.error("Deposit failed for transferId: {}. Initiating compensation...", transfer.getId().value(), e);

                // ステップ4a: 補償トランザクション
                // 入金に失敗した場合、すでに行われた出金を取り消すため、同額を振込元口座に入金し直す。
                depositUseCase.deposit(transfer.getSourceAccountId(), transfer.getMoney());

                // Transferの状態をFAILEDにしてSagaを終了
                transfer.fail();
                transferRepository.save(transfer);
                log.info("Transfer saga failed and compensated for transferId: {}", transfer.getId().value());
            }

        } catch (Exception e) {
            log.error("Withdraw failed for transferId: {}. Marking as failed.", transfer.getId().value(), e);

            // ステップ4b: Sagaの失敗
            // 最初のステップである出金に失敗した場合、補償処理は不要。
            // Transferの状態をFAILEDにしてSagaを終了する。
            transfer.fail();
            transferRepository.save(transfer);
            log.info("Transfer saga failed for transferId: {}", transfer.getId().value());
        }
    }
} 