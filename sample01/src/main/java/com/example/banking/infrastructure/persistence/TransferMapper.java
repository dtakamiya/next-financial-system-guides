package com.example.banking.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 振込データの永続化を担うMyBatis Mapperインターフェース。
 *
 * @see AccountMapper
 */
@Mapper
public interface TransferMapper {
    Optional<TransferData> findById(@Param("id") UUID id);
    void insert(TransferData transferData);
    int update(TransferData transferData);
} 