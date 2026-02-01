package com.example.board.auth.service.config.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 트랜잭션(@Transactional)보다 먼저 실행되어야 함
public class DataSourceAspect {

    @Around("@annotation(com.example.board.auth.service.config.datasource.ForcePrimary)")
    public Object forcePrimaryDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        return ScopedValue.where(ScopedValueDataSourceContextHolder.ROUTING_KEY, DataSourceType.READ_WRITE)
                .call(joinPoint::proceed);
    }
}
