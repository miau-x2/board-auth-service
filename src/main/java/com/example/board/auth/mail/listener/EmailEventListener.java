package com.example.board.auth.mail.listener;

import com.example.board.auth.mail.dto.MailContext;
import com.example.board.auth.mail.event.EmailSendEvent;
import com.example.board.auth.mail.result.SendEmailResult;
import com.example.board.auth.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {
    private static final int OTP_EXPIRES_IN_MINUTES = 5;
    private final EmailService emailService;

    @Async("mailTaskExecutor")
    @EventListener
    public void handleEmailSendEvent(EmailSendEvent event) {
        log.info("메일 발송 이벤트 수신. email={}", event.email());
        try {
            var result = emailService.sendEmail(buildMailContext(event.email(), event.otp()));
            switch (result) {
                case SendEmailResult.Success _ ->
                        log.info("메일 발송 완료. email={}", event.email());
                case SendEmailResult.AuthenticationFailed(var throwable) ->
                        log.error("메일 서버 인증 실패. email={}", event.email(), throwable);
                case SendEmailResult.ComposeFailed(var throwable) ->
                        log.error("메일 본문 구성 실패. email={}", event.email(), throwable);
                case SendEmailResult.SendFailed(var throwable) ->
                        log.warn("메일 전송 실패. email={}", event.email(), throwable);
            }
        } catch (Exception e) {
            log.error("메일 발송 중 예외 발생. email={}", event.email(), e);
        }
    }

    private MailContext buildMailContext(String email, String otp) {
        return new MailContext(email,"회원가입 이메일 인증번호 안내", otp, OTP_EXPIRES_IN_MINUTES);
    }
}
