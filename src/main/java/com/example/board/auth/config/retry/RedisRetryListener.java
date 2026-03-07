package com.example.board.auth.config.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.retry.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisRetryListener implements RetryListener {

    @Override
    public void onRetryableExecution(RetryPolicy retryPolicy, Retryable<?> retryable, RetryState retryState) {
        var retryCount = retryState.getRetryCount();
        if(retryState.isSuccessful()) {
            if(retryCount > 0) {
                log.info("[RETRY]: SUCCESS, [ATTEMPT]: {}", retryCount);
            }
        }
        else {
            log.info("[RETRY]: FAILED, [ATTEMPT]: {}", retryCount);
        }
    }

    @Override
    public void onRetryPolicyExhaustion(RetryPolicy retryPolicy, Retryable<?> retryable, RetryException exception) {
        log.error("[RETRY]: EXHAUSTED, [ATTEMPT]: {}", exception.getRetryCount(), exception);
    }

    @Override
    public void onRetryPolicyInterruption(RetryPolicy retryPolicy, Retryable<?> retryable, RetryException exception) {
        log.error("[RETRY]: INTERRUPTED, [ATTEMPT]: {}", exception.getRetryCount(), exception);
    }

    @Override
    public void onRetryPolicyTimeout(RetryPolicy retryPolicy, Retryable<?> retryable, RetryException exception) {
        log.error("[RETRY]: TIMEOUT, [ATTEMPT]: {}", exception.getRetryCount(), exception);
    }
}
