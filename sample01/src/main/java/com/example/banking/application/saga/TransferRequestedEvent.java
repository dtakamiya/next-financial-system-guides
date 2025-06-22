package com.example.banking.application.saga;

import com.example.banking.domain.transfer.TransferId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 振込依頼が行われたことを示すドメインイベント。
 * このイベントは、振込依頼ユースケースが完了したときに発行され、
 * TransferSagaを非同期に起動するトリガーとして機能します。
 */
@Getter
public class TransferRequestedEvent extends ApplicationEvent {

    /**
     * イベントの対象となる振込ID。
     */
    @Getter
    private final TransferId transferId;

    /**
     * 振込リクエストイベントを生成します。
     *
     * @param source     イベントソース (通常はイベントを発行したコンポーネント)
     * @param transferId 関連する振込のID
     */
    public TransferRequestedEvent(Object source, TransferId transferId) {
        super(source);
        this.transferId = transferId;
    }
} 