package com.example.banking.infrastructure.persistence;

import com.example.banking.domain.account.AccountId;
import com.example.banking.domain.account.Money;
import com.example.banking.domain.transfer.Transfer;
import com.example.banking.domain.transfer.TransferId;
import com.example.banking.domain.transfer.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.Currency;
import java.util.Optional;

/**
 * TransferRepositoryのMyBatisによる実装。
 *
 * @see MyBatisAccountRepository
 */
@Repository
@RequiredArgsConstructor
public class MyBatisTransferRepository implements TransferRepository {

    /**
     * MyBatisのTransferMapperインターフェース。
     * このマッパーを通じてデータベースとのやり取りが行われます。
     */
    private final TransferMapper transferMapper;

    @Override
    public Optional<Transfer> findById(TransferId id) {
        return transferMapper.findById(id.value()).map(this::toDomain);
    }

    @Override
    public void save(Transfer transfer) {
        TransferData data = toData(transfer);
        if (transfer.getVersion() == null) {
            transferMapper.insert(data);
        } else {
            int updatedRows = transferMapper.update(data);
            if (updatedRows == 0) {
                throw new OptimisticLockingFailureException("Transfer has been updated by another transaction: " + transfer.getId().value());
            }
        }
    }

    private Transfer toDomain(TransferData data) {
        return new Transfer(
                new TransferId(data.getId()),
                new AccountId(data.getSourceAccountId()),
                new AccountId(data.getDestinationAccountId()),
                new Money(data.getMoneyAmount(), Currency.getInstance(data.getMoneyCurrency())),
                data.getStatus(),
                data.getVersion()
        );
    }

    private TransferData toData(Transfer domain) {
        return new TransferData(
                domain.getId().value(),
                domain.getSourceAccountId().value(),
                domain.getDestinationAccountId().value(),
                domain.getMoney().amount(),
                domain.getMoney().currency().getCurrencyCode(),
                domain.getStatus(),
                domain.getVersion()
        );
    }
} 