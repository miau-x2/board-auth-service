package com.example.board.auth.mail.service;

import com.example.board.auth.mail.MailProperties;
import com.example.board.auth.mail.dto.MailContext;
import com.example.board.auth.mail.exception.MailSendFailedException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {
    @Mock
    private JavaMailSender javaMailSender;
    private MailProperties mailProperties;
    private RetryTemplate retryTemplate;
    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        mailProperties = new MailProperties(new MailProperties.From("address", "service-name"));
        var retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ZERO)
                .includes(MailSendFailedException.class)
                .build();
        retryTemplate = new RetryTemplate(retryPolicy);
        emailSender = new ThrottledEmailSender(javaMailSender, mailProperties, retryTemplate);
    }

    @Test
    @DisplayName("메일 전송 - 성공")
    void should_SendMimeMessage_When_InputIsValid() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", true);
        var message = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        emailSender.send(mailContext);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(message);
    }

    @Test
    @DisplayName("메일 전송 - 성공(재시도 후 성공)")
    void should_Succeed_AfterRetries_When_TransientErrorOccurs() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", true);
        var message = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        doThrow(MailSendException.class)
                .doThrow(MailSendException.class)
                .doNothing()
                .when(javaMailSender)
                .send(message);

        emailSender.send(mailContext);

        verify(javaMailSender, times(3)).createMimeMessage();
        verify(javaMailSender, times(3)).send(message);
    }

    @Test
    @DisplayName("메일 전송 - 실패(파싱 에러)")
    void should_ThrowMailSendFailedException_When_ParseExceptionOccurs() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", true);
        var message = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        doThrow(MailParseException.class)
                .when(javaMailSender)
                .send(message);

        assertThatThrownBy(() -> emailSender.send(mailContext))
                        .isExactlyInstanceOf(MailSendFailedException.class);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(message);
    }

    @Test
    @DisplayName("메일 전송 - 실패(준비 에러)")
    void should_ThrowMailSendFailedException_When_PreparationExceptionOccurs() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", true);
        var message = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        doThrow(MailPreparationException.class)
                .when(javaMailSender)
                .send(message);

        assertThatThrownBy(() -> emailSender.send(mailContext))
                .isExactlyInstanceOf(MailSendFailedException.class);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(message);
    }

    @Test
    @DisplayName("메일 전송 - 실패(인증 에러)")
    void should_ThrowMailSendFailedException_When_AuthenticationExceptionOccurs() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", true);
        var message = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        doThrow(MailAuthenticationException.class)
                .when(javaMailSender)
                .send(message);

        assertThatThrownBy(() -> emailSender.send(mailContext))
                .isExactlyInstanceOf(MailSendFailedException.class);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(message);
    }

    @Test
    @DisplayName("메일 전송 - 실패(재시도 횟수 소진)")
    void should_ThrowMailSendFailedException_When_RetryExhausted() {
        var mailContext = new MailContext("user@example.com", "제목", "123456", true);
        var message = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        doThrow(MailSendException.class)
                .when(javaMailSender)
                .send(message);

        assertThatThrownBy(() -> emailSender.send(mailContext))
                .isExactlyInstanceOf(MailSendFailedException.class);

        verify(javaMailSender, times(3)).createMimeMessage();
        verify(javaMailSender, times(3)).send(message);
    }
}