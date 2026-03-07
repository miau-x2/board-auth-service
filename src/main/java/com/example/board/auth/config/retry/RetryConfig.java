package com.example.board.auth.config.retry;

import com.example.board.auth.authentication.token.exception.RedisConcurrencyFailureException;
import com.example.board.auth.commons.exception.RetryableRemoteException;
import com.example.board.auth.mail.exception.MailSendFailedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class RetryConfig {

    @Bean
    public RetryTemplate sendEmailRetryTemplate(@Qualifier("sendEmailRetryListener") RetryListener retryListener) {
        var retryPolicy = RetryPolicy.builder()
                .maxRetries(3)
                .delay(Duration.ofMillis(200))
                .multiplier(2.0)
                .maxDelay(Duration.ofSeconds(1))
                .jitter(Duration.ofMillis(50))
                .timeout(Duration.ofSeconds(2))
                .includes(MailSendFailedException.class)
                .build();
        var retryTemplate = new RetryTemplate(retryPolicy);
        retryTemplate.setRetryListener(retryListener);
        return retryTemplate;
    }

    @Bean
    public RetryTemplate memberApiRetryTemplate(@Qualifier("memberApiRetryListener") RetryListener retryListener) {
        var retryPolicy = RetryPolicy.builder()
                .maxRetries(3)
                .delay(Duration.ofMillis(100))
                .multiplier(2)
                .maxDelay(Duration.ofMillis(500))
                .jitter(Duration.ofMillis(20))
                .timeout(Duration.ofSeconds(1))
                .includes(RetryableRemoteException.class)
                .build();
        var retryTemplate = new RetryTemplate(retryPolicy);
        retryTemplate.setRetryListener(retryListener);
        return retryTemplate;
    }

    @Bean
    public RetryTemplate redisRetryTemplate(@Qualifier("redisRetryListener") RetryListener retryListener) {
        var retryPolicy = RetryPolicy.builder()
                .maxRetries(3)
                .delay(Duration.ofMillis(20))
                .multiplier(2)
                .maxDelay(Duration.ofMillis(100))
                .jitter(Duration.ofMillis(5))
                .timeout(Duration.ofMillis(200))
                .includes(RedisConcurrencyFailureException.class)
                .build();
        var retryTemplate = new RetryTemplate(retryPolicy);
        retryTemplate.setRetryListener(retryListener);
        return retryTemplate;
    }
}
