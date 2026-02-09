package com.example.board.auth.config.datasource;

import java.util.Optional;

public interface DataSourceContextHolder {
    Optional<DataSourceType> get();
}
