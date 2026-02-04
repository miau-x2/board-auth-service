package com.example.board.auth.mail.service;

import com.example.board.auth.commons.utils.EmailDomainPolicy;
import com.example.board.auth.mail.event.EmailSendEvent;
import com.example.board.auth.mail.repository.EmailAuthenticationRepository;
import com.example.board.auth.mail.result.EmailAuthenticationResult;
import com.example.board.auth.mail.result.SaveOtpResult;
import com.example.board.auth.mail.service.command.EmailSendCommand;
import com.example.board.auth.mail.service.command.EmailVerifyCommand;
import com.example.board.auth.token.OtpGenerator;
import com.example.board.auth.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAuthenticationService {
    private final OtpGenerator otpGenerator;
    private final TokenGenerator tokenGenerator;
    private final EmailAuthenticationRepository emailAuthenticationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EmailAuthenticationResult.SendOtp sendOtp(EmailSendCommand command) {
        if(!EmailDomainPolicy.isDomainAllowed(command.email())) {
            return new EmailAuthenticationResult.SendOtp.EmailDomainNotAllowed();
        }
       // otp 저장
        var result = emailAuthenticationRepository.saveSignupOtp(command.email(), otpGenerator::generate);
        return switch (result) {
            case SaveOtpResult.Signup.Success(var otp) -> {
                // 메일 발송 이벤트 발행
                eventPublisher.publishEvent(new EmailSendEvent(command.email(), otp));
                yield new EmailAuthenticationResult.SendOtp.Success();
            }
            case SaveOtpResult.Signup.Cooldown(var retryAfterSeconds) -> {
                log.error("메일 재전송 쿨다운 설정. 남은 대기시간: {}초", retryAfterSeconds);
                yield new EmailAuthenticationResult.SendOtp.TooManyRequests(retryAfterSeconds);
            }
        };
    }

    public EmailAuthenticationResult.VerifyOtp verifyOtp(EmailVerifyCommand command) {
        // otp 검증 - 실패
        var storedOtp = emailAuthenticationRepository.getOtp(command.email());
        if(storedOtp == null || storedOtp.isBlank()) {
            return new EmailAuthenticationResult.VerifyOtp.Expired();
        }
        if(!storedOtp.equals(command.otp())) {
            return new EmailAuthenticationResult.VerifyOtp.Invalid();
        }
        // otp 검증 - 성공
        // 검증 성공한 otp 삭제
        emailAuthenticationRepository.deleteOtp(command.email());
        // 10분간 유효한 회원 가입 토큰 발행
        var token = tokenGenerator.generate();
        // 토큰 저장
        emailAuthenticationRepository.saveSignupToken(token, command.email());
        return new EmailAuthenticationResult.VerifyOtp.Success(token);
    }
}
