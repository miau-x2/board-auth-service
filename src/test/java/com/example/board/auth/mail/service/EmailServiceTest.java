package com.example.board.auth.mail.service;

import com.example.board.auth.mail.dto.MailContext;
import com.example.board.auth.mail.exception.MailAuthenticationFailedException;
import com.example.board.auth.mail.exception.MailComposeFailedException;
import com.example.board.auth.mail.exception.MailSendFailedException;
import com.example.board.auth.mail.result.SendEmailResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private SpringTemplateEngine springTemplateEngine;
    @Mock
    private EmailSender emailSender;
    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("이메일 전송 - 성공")
    void send_email_success() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", 5);
        var text = "text";
        when(springTemplateEngine.process(anyString(), any(Context.class))).thenReturn(text);

        var actual = emailService.sendEmail(mailContext);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.Success.class);

        verify(springTemplateEngine).process(anyString(), any(Context.class));
        verify(emailSender).send(any(MailContext.class), anyString());
    }

    @Test
    @DisplayName("이메일 전송 - 실패(템플릿 구성 실패)")
    void send_fail_when_throw_template_engine_process_exception() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", 5);
        when(springTemplateEngine.process(anyString(), any(Context.class))).thenThrow(RuntimeException.class);

        var actual = emailService.sendEmail(mailContext);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.ComposeFailed.class);

        verify(springTemplateEngine).process(anyString(), any(Context.class));
        verify(emailSender, never()).send(any(MailContext.class), anyString());
    }

    @Test
    @DisplayName("이메일 전송 - 실패(메일 서버 인증 실패)")
    void send_fail_when_throw_mail_authentication_failed_exception() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", 5);
        var text = "text";
        when(springTemplateEngine.process(anyString(), any(Context.class))).thenReturn(text);
        doThrow(MailAuthenticationFailedException.class)
                .when(emailSender).send(mailContext, text);

        var actual = emailService.sendEmail(mailContext);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.AuthenticationFailed.class);

        verify(springTemplateEngine).process(anyString(), any(Context.class));
        verify(emailSender).send(mailContext, text);
    }

    @Test
    @DisplayName("이메일 전송 - 실패(메일 구성 실패)")
    void send_fail_when_throw_mail_compose_failed_exception() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", 5);
        var text = "text";
        when(springTemplateEngine.process(anyString(), any(Context.class))).thenReturn(text);
        doThrow(MailComposeFailedException.class)
                .when(emailSender).send(mailContext, text);

        var actual = emailService.sendEmail(mailContext);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.ComposeFailed.class);

        verify(springTemplateEngine).process(anyString(), any(Context.class));
        verify(emailSender).send(mailContext, text);
    }

    @Test
    @DisplayName("이메일 전송 - 실패(메일 전송 실패)")
    void send_fail_when_throw_mail_send_failed_exception() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", 5);
        var text = "text";
        when(springTemplateEngine.process(anyString(), any(Context.class))).thenReturn(text);
        doThrow(MailSendFailedException.class)
                .when(emailSender).send(mailContext, text);

        var actual = emailService.sendEmail(mailContext);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.SendFailed.class);

        verify(springTemplateEngine).process(anyString(), any(Context.class));
        verify(emailSender).send(mailContext, text);
    }
}