package com.example.board.auth.service.config.datasource;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RequiredArgsConstructor
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {
    private final DataSourceContextHolder dataSourceContextHolder;

    @Override
    protected Object determineCurrentLookupKey() {
        return dataSourceContextHolder
                .get()
                .orElseGet(() ->
                        TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? DataSourceType.READ_ONLY : DataSourceType.READ_WRITE);

    }
}