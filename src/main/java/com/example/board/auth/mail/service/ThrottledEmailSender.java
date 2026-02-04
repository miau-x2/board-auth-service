package com.example.board.auth.mail.service;

import com.example.board.auth.mail.MailProperties;
import com.example.board.auth.mail.dto.MailContext;
import com.example.board.auth.mail.exception.MailAuthenticationFailedException;
import com.example.board.auth.mail.exception.MailComposeFailedException;
import com.example.board.auth.mail.exception.MailSendFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ThrottledEmailSender implements EmailSender {
    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;
    private final RetryTemplate retryTemplate;

    public ThrottledEmailSender(JavaMailSender javaMailSender, MailProperties mailProperties, @Qualifier("sendEmailRetryTemplate") RetryTemplate retryTemplate) {
        this.javaMailSender = javaMailSender;
        this.mailProperties = mailProperties;
        this.retryTemplate = retryTemplate;
    }

    @ConcurrencyLimit(value = 20, policy = ConcurrencyLimit.ThrottlePolicy.BLOCK)
    public void send(MailContext mailContext, String text) {
        try {
            retryTemplate.execute(() -> {
                process(mailContext, text);
                return null;
            });
        } catch (RetryException e) {
            throw new MailSendFailedException(e);
        }
    }

    private void process(MailContext mailContext, String text) {
        var message = javaMailSender.createMimeMessage();
        try {
            var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            try {
                helper.setFrom(new InternetAddress(mailProperties.from().address(), mailProperties.from().name(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException _) {
                log.warn("메일 주소 인코딩 실패. from={}", mailProperties.from());
                helper.setFrom(mailProperties.from().address());
            }
            helper.setTo(mailContext.to());
            helper.setSubject(mailContext.subject());
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (MessagingException | MailParseException | MailPreparationException e) {
            // 재시도 X
            throw new MailComposeFailedException(e);
        } catch (MailAuthenticationException e) {
            // 재시도 X
            throw new MailAuthenticationFailedException(e);
        } catch (MailSendException e) {
            // 재시도 O
            throw new MailSendFailedException(e);
        }
    }
}
