package com.example.board.auth.mail.service;

import com.example.board.auth.mail.EmailType;
import com.example.board.auth.mail.dto.MailContext;
import com.example.board.auth.mail.exception.MailAuthenticationFailedException;
import com.example.board.auth.mail.exception.MailComposeFailedException;
import com.example.board.auth.mail.exception.MailSendFailedException;
import com.example.board.auth.mail.result.SendEmailResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailService {
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final EmailSender throttledEmailSender;

    public SendEmailResult sendEmail(EmailType emailType, String email, String otp, long expiresInMinutes) {
        String text;
        try {
            text = emailTemplateRenderer.render(emailType, otp, expiresInMinutes);
        } catch (Exception e) {
            // 재시도 X
            return new SendEmailResult.ComposeFailed(e);
        }
        try {
            throttledEmailSender.send(new MailContext(email, emailType.getSubject(), text, emailType.isHtml()));
            return new SendEmailResult.Success();
        } catch (MailAuthenticationFailedException e) {
            return new SendEmailResult.AuthenticationFailed(e);
        } catch (MailComposeFailedException e) {
            return new SendEmailResult.ComposeFailed(e);
        } catch (MailSendFailedException e) {
            return new SendEmailResult.SendFailed(e);
        }
    }
}
