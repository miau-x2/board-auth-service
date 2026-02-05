package com.example.board.auth.mail.service;

import com.example.board.auth.mail.AuthEmailType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private EmailTemplateRenderer emailTemplateRenderer;
    @Mock
    private EmailSender emailSender;
    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("이메일 전송 - 성공")
    void should_ReturnSuccess_When_MailSentSuccessfully() {
        var emailType = AuthEmailType.SIGNUP;
        var otp = "123456";
        var expiresInMinutes = 5;
        var html = "html";
        when(emailTemplateRenderer.render(emailType, otp, expiresInMinutes))
                .thenReturn(html);

        var actual = emailService.sendEmail(AuthEmailType.SIGNUP, "test@example.com", "123456", 5);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.Success.class);

        verify(emailTemplateRenderer).render(emailType, otp, expiresInMinutes);
        verify(emailSender).send(any(MailContext.class));
    }

    @Test
    @DisplayName("이메일 전송 - 실패(템플릿 처리 실패)")
    void should_ReturnComposeFailed_When_TemplateProcessingFails() {
        var emailType = AuthEmailType.SIGNUP;
        var otp = "123456";
        var expiresInMinutes = 5;
        when(emailTemplateRenderer.render(emailType, otp, expiresInMinutes))
                .thenThrow(RuntimeException.class);

        var actual = emailService.sendEmail(AuthEmailType.SIGNUP, "test@example.com", "123456", 5);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.ComposeFailed.class);

        verify(emailTemplateRenderer).render(emailType, otp, expiresInMinutes);
        verify(emailSender, never()).send(any(MailContext.class));
    }

    @Test
    @DisplayName("이메일 전송 - 실패(메일 서버 인증 실패)")
    void should_ReturnAuthenticationFailed_When_MailServerAuthenticationFails() {
        var emailType = AuthEmailType.SIGNUP;
        var otp = "123456";
        var expiresInMinutes = 5;
        var html = "html";
        when(emailTemplateRenderer.render(emailType, otp, expiresInMinutes))
                .thenReturn(html);
        doThrow(MailAuthenticationFailedException.class)
                .when(emailSender).send(any(MailContext.class));

        var actual = emailService.sendEmail(AuthEmailType.SIGNUP, "test@example.com", "123456", 5);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.AuthenticationFailed.class);

        verify(emailTemplateRenderer).render(emailType, otp, expiresInMinutes);
        verify(emailSender).send(any(MailContext.class));
    }

    @Test
    @DisplayName("이메일 전송 - 실패(메일 구성 실패)")
    void should_ReturnComposeFailed_When_MailCompositionFails() {
        var emailType = AuthEmailType.SIGNUP;
        var otp = "123456";
        var expiresInMinutes = 5;
        var html = "html";
        when(emailTemplateRenderer.render(emailType, otp, expiresInMinutes))
                .thenReturn(html);
        doThrow(MailComposeFailedException.class)
                .when(emailSender).send(any(MailContext.class));

        var actual = emailService.sendEmail(AuthEmailType.SIGNUP, "test@example.com", "123456", 5);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.ComposeFailed.class);

        verify(emailTemplateRenderer).render(emailType, otp, expiresInMinutes);
        verify(emailSender).send(any(MailContext.class));
    }

    @Test
    @DisplayName("이메일 전송 - 실패(메일 전송 실패)")
    void should_ReturnSendFailed_When_MailSendingFails() {
        var emailType = AuthEmailType.SIGNUP;
        var otp = "123456";
        var expiresInMinutes = 5;
        var html = "html";
        when(emailTemplateRenderer.render(emailType, otp, expiresInMinutes))
                .thenReturn(html);
        doThrow(MailSendFailedException.class)
                .when(emailSender).send(any(MailContext.class));

        var actual = emailService.sendEmail(AuthEmailType.SIGNUP, "test@example.com", "123456", 5);
        assertThat(actual)
                .isExactlyInstanceOf(SendEmailResult.SendFailed.class);

        verify(emailTemplateRenderer).render(emailType, otp, expiresInMinutes);
        verify(emailSender).send(any(MailContext.class));
    }
}