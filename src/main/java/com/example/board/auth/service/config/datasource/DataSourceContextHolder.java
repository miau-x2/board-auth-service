package com.example.board.auth.service.config.datasource;

import java.util.Optional;

public interface DataSourceContextHolder {
    Optional<DataSourceType> get();
}
