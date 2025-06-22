package com.example.banking.domain.transfer;

import java.util.Optional;

public interface TransferRepository {

    /**
     * 新しい振込を永続化します。
     * @param transfer 保存する振込アグリゲート
     */
    void save(Transfer transfer);

    /**
     * 振込IDで振込を検索します。
     * @param id 検索する振込のID
     * @return 見つかった振込。見つからない場合はOptional.empty()
     */
    Optional<Transfer> findById(TransferId id);

    /**
     * 振込を更新します。
     * @param transfer 更新する振込アグリゲート
     */
    void update(Transfer transfer);
} 