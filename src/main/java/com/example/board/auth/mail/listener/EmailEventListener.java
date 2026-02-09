package com.example.board.auth.mail.listener;

import com.example.board.auth.mail.EmailAuthenticationProperties;
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
    private final EmailService emailService;
    private final EmailAuthenticationProperties authenticationProperties;

    @Async("mailTaskExecutor")
    @EventListener
    public void handleEmailSendEvent(EmailSendEvent event) {
        log.info("메일 발송 이벤트 수신. email={}", event.email());
        try {
            var result = emailService.sendEmail(
                    event.emailType(), event.email(), event.otp(), authenticationProperties.email().otp().validity().toMinutes());
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
}
