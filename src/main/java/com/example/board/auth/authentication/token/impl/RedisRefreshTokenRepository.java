package com.example.board.auth.authentication.token.impl;

import com.example.board.auth.authentication.config.TokenProperties;
import com.example.board.auth.authentication.token.RefreshTokenRepository;
import com.example.board.auth.authentication.token.RotateOrReplayRefreshTokenResult;
import com.example.board.auth.authentication.token.exception.RedisConcurrencyFailureException;
import com.example.board.auth.authentication.token.exception.RefreshTokenRemovalFailedException;
import com.example.board.auth.authentication.token.exception.RefreshTokenRotateFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class RedisRefreshTokenRepository implements RefreshTokenRepository {
    private static final String TOKEN_VALUE_FIELD = "rt";
    private static final String EXPIRES_AT_FIELD = "exp";
    private final StringRedisTemplate stringRedisTemplate;
    private final RetryTemplate retryTemplate;
    private final TokenProperties tokenProperties;

    public RedisRefreshTokenRepository(StringRedisTemplate stringRedisTemplate, @Qualifier("redisRetryTemplate") RetryTemplate retryTemplate, TokenProperties tokenProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.retryTemplate = retryTemplate;
        this.tokenProperties = tokenProperties;
    }

    @Override
    public void save(Long memberId, RefreshToken refreshToken) {
        var timeToLive = validateTimeToLive(refreshToken.getExpiresAt());

        var key = refreshTokenKey(memberId);
        var newHashedToken = hashToken(refreshToken.getTokenValue());
        stringRedisTemplate.opsForValue().set(key, newHashedToken, timeToLive);
    }

    @Override
    public void remove(Long memberId, String refreshTokenValue) {
        try {
            retryTemplate.execute(() -> {
                var key = refreshTokenKey(memberId);
                var requestTokenHash = hashToken(refreshTokenValue);
                stringRedisTemplate.execute(new SessionCallback<Void>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <K, V> Void execute(RedisOperations<K, V> operations) throws DataAccessException {
                        var ops = (RedisOperations<String, String>) operations;
                        ops.watch(key);
                        try {
                            var savedTokenHash = ops.opsForValue().get(key);
                            if (savedTokenHash == null || !savedTokenHash.equals(requestTokenHash)) {
                                return null;
                            }
                            ops.multi();
                            ops.delete(key);

                            var result = ops.exec();
                            if(result.isEmpty()) {
                                log.error("key:{}에 저장된 토큰 변경", key);
                                throw new RedisConcurrencyFailureException("키에 저장된 토큰 변경으로 리프레시 토큰이 변경되었습니다.");
                            }
                            return null;
                        } finally {
                            try {
                                ops.unwatch();
                            } catch (Exception _) {}
                        }
                    }
                });
                return null;
            });
        } catch (RetryException e) {
            throw new RefreshTokenRemovalFailedException(e);
        }
    }

    @Override
    public RotateOrReplayRefreshTokenResult rotateOrReplay(Long memberId, String refreshTokenValue, RefreshToken candidateRefreshToken) {
        var timeToLive = validateTimeToLive(candidateRefreshToken.getExpiresAt());
        try {
            return retryTemplate.execute(() -> stringRedisTemplate.execute(new SessionCallback<>() {
                @Override
                @SuppressWarnings("unchecked")
                public <K, V> RotateOrReplayRefreshTokenResult execute(RedisOperations<K, V> operations) throws DataAccessException {
                    var ops = (RedisOperations<String, String>) operations;
                    var tokenKey = refreshTokenKey(memberId);
                    var requestTokenHash = hashToken(refreshTokenValue);
                    ops.watch(tokenKey);
                    try {
                        // 현재 저장된 리프레시 토큰 조회
                        var savedTokenHash = ops.opsForValue().get(tokenKey);
                        // 저장된 리프레시 토큰이 없으면 리프레시 토큰 만료 되었거나 잘못된 요청
                        if(savedTokenHash == null) {
                            return new RotateOrReplayRefreshTokenResult.Invalid();
                        }
                        // 저장된 리프레시 토큰은 비어있을 수 없으므로 예외 발생
                        if(savedTokenHash.isEmpty()) {
                            throw new IllegalStateException("저장된 리프레시 토큰 해시값은 비어있을 수 없습니다.");
                        }
                        // 저장된 리프레시 토큰과 재발급 요청 리프레시 토큰 비교
                        // 같은 경우
                        // -> 새 리프레시 토큰으로 rotate
                        // -> 이전 리프레시 토큰으로 새로운 리프레시 토큰 grace에 저장 후 Success 반환
                        if(savedTokenHash.equals(requestTokenHash)) {
                            return rotate(memberId, tokenKey, requestTokenHash, candidateRefreshToken, timeToLive, ops);
                        }
                        // 다른 경우
                        // -> 이전 리프레시 토큰으로 grace replay
                        // -> 리프레시 토큰 존재하면  Success 반환 없으면 Invalid 반환
                        return replay(memberId, requestTokenHash, ops);
                    } finally {
                        try {
                            ops.unwatch();
                        } catch (Exception _) {}
                    }
                }
            }));
        } catch (RetryException e) {
            throw new RefreshTokenRotateFailedException(e);
        }
    }

    private String refreshTokenKey(Long memberId) {
        return "auth:token:refresh:member:%s".formatted(memberId);
    }

    private String refreshTokenGraceKey(Long memberId, String prevTokenHash) {
        return "auth:token:refresh:member:%s:grace:%s".formatted(memberId, prevTokenHash);
    }

    // -> 새 리프레시 토큰으로 rotate
    // -> 이전 리프레시 토큰으로 새로운 리프레시 토큰 grace에 저장 후 Success 반환
    private RotateOrReplayRefreshTokenResult rotate(Long memberId, String tokenKey, String requestTokenHash, RefreshToken refreshToken, Duration timeToLive, RedisOperations<String, String> ops) {
        var tokenGraceKey = refreshTokenGraceKey(memberId, requestTokenHash);
        var newTokenHash = hashToken(refreshToken.getTokenValue());
        var refreshTokenEntries = Map.of(
                TOKEN_VALUE_FIELD, refreshToken.getTokenValue(),
                EXPIRES_AT_FIELD, String.valueOf(refreshToken.getExpiresAt().toEpochMilli())
        );
        ops.multi();
        ops.opsForValue().set(tokenKey, newTokenHash, timeToLive);
        ops.opsForHash().putAndExpire(tokenGraceKey, refreshTokenEntries, RedisHashCommands.HashFieldSetOption.IF_NONE_EXIST, Expiration.from(tokenProperties.refresh().opaque().gracePeriod()));
        var rotateTxResult = ops.exec();
        if(rotateTxResult.isEmpty()) {
            throw new RedisConcurrencyFailureException("키에 저장된 토큰 변경으로 리프레시 토큰이 변경되었습니다.");
        }
        return new RotateOrReplayRefreshTokenResult.Success(refreshToken);
    }

    // -> 이전 리프레시 토큰으로 grace replay
    // -> 리프레시 토큰 존재하면 Success 반환 없으면 Invalid 반환
    private RotateOrReplayRefreshTokenResult replay(Long memberId, String requestTokenHash, RedisOperations<String, String> ops) {
        var tokenGraceKey = refreshTokenGraceKey(memberId, requestTokenHash);
        var refreshTokenEntries = ops.opsForHash().entries(tokenGraceKey);
        if(refreshTokenEntries.isEmpty()) {
            log.warn("회원:{}의 refresh token grace period가 만료되었습니다.", memberId);
            return new RotateOrReplayRefreshTokenResult.Invalid();
        }
        var refreshTokenValue = (String) refreshTokenEntries.get(TOKEN_VALUE_FIELD);
        var expiresAtValue = (String) refreshTokenEntries.get(EXPIRES_AT_FIELD);
        if(refreshTokenValue == null || expiresAtValue == null) {
            throw new IllegalStateException("저장된 리프레시 토큰값과 유효기간은 null일 수 없습니다.");
        }
        if(refreshTokenValue.isEmpty() || expiresAtValue.isEmpty()) {
            throw new IllegalStateException("저장된 리프레시 토큰값과 유효기간은 비어있을 수 없습니다.");
        }
        try {
            var expiresAt = Instant.ofEpochMilli(Long.parseLong(expiresAtValue));
            var refreshToken = new RefreshToken(refreshTokenValue, expiresAt);
            return new RotateOrReplayRefreshTokenResult.Success(refreshToken);
        } catch (NumberFormatException _) {
            throw new IllegalStateException("저장된 만료시간 값이 숫자 형식이 아닐 수 없습니다.");
        }
    }

    private Duration validateTimeToLive(Instant expiresAt) {
        var timeToLive = Duration.between(Instant.now(), expiresAt);
        if(timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalArgumentException("리프레시 토큰의 만료시간은 현재 시각 이후여야 합니다.");
        }
        return timeToLive;
    }

    private String hashToken(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hashedToken = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashedToken);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}
