package com.example.board.auth.mail.repository.impl;

import com.example.board.auth.mail.EmailAuthenticationProperties;
import com.example.board.auth.mail.repository.EmailAuthenticationRepository;
import com.example.board.auth.mail.result.SaveOtpResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisEmailAuthenticationRepository implements EmailAuthenticationRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private final EmailAuthenticationProperties emailAuthenticationProperties;

    @Override
    public SaveOtpResult.Signup saveSignupOtp(String email, Supplier<String> supplier) {
        var cooldownKey = signupCooldownKey(email);
        var isAllowed = stringRedisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", emailAuthenticationProperties.email().otp().cooldown());
        if(Boolean.FALSE.equals(isAllowed)) {
            var remainSeconds = stringRedisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
            var retryAfterSeconds = remainSeconds == null || remainSeconds < 0 ? emailAuthenticationProperties.email().otp().cooldown().toSeconds() : remainSeconds;
            return new SaveOtpResult.Signup.Cooldown(retryAfterSeconds);
        }
        var otp = supplier.get();
        log.info("otp 발급");
        var otpKey = signupOtpKey(email);
        try {
            stringRedisTemplate.opsForValue().set(otpKey, otp, emailAuthenticationProperties.email().otp().validity());
            log.info("otp 저장 완료");
            return new SaveOtpResult.Signup.Success(otp);
        } catch (Exception e) {
            log.error("otp 저장 중 예외 발생. 쿨다운 해제 시도", e);
            try {
                stringRedisTemplate.delete(cooldownKey);
            } catch (Exception ex) {
                log.warn("쿨다운 해제 실패.", ex);
            }
            throw e;
        }
    }

    @Override
    public void saveSignupToken(String token, String email) {
        var signupTokenKey = signupTokenKey(token);
        stringRedisTemplate.opsForValue().set(signupTokenKey, email, emailAuthenticationProperties.signup().token().validity());
        log.info("인증 완료된 이메일 저장: {}", email);
    }

    @Override
    public String getOtp(String email) {
        return stringRedisTemplate.opsForValue().get(signupOtpKey(email));
    }

    @Override
    public String useSignupToken(String token) {
        var signupTokenKey = signupTokenKey(token);
        var email = stringRedisTemplate.opsForValue().getAndDelete(signupTokenKey);
        log.info("이메일 인증 토큰 사용. 토큰: {}, 이메일: {}", token, email);
        return email;
    }

    @Override
    public void deleteOtp(String email) {
        stringRedisTemplate.delete(signupOtpKey(email));
    }

    private String signupOtpKey(String email) {
        return "auth:otp:signup:%s".formatted(email);
    }

    private String signupCooldownKey(String email) {
        return "auth:otp:signup:cooldown:%s".formatted(email);
    }

    private String signupTokenKey(String token) {
        return "auth:proof:signup:%s".formatted(token);
    }
}
