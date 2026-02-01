package com.example.board.auth.service.config.datasource;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ScopedValueDataSourceContextHolder implements DataSourceContextHolder {
    public static final ScopedValue<DataSourceType> ROUTING_KEY = ScopedValue.newInstance();

    @Override
    public Optional<DataSourceType> get() {
        return ROUTING_KEY.isBound() ? Optional.of(ROUTING_KEY.get()) : Optional.empty();
    }
}
