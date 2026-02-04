package com.example.board.auth.mail.service;

import com.example.board.auth.mail.dto.MailContext;
import com.example.board.auth.mail.exception.MailAuthenticationFailedException;
import com.example.board.auth.mail.exception.MailComposeFailedException;
import com.example.board.auth.mail.exception.MailSendFailedException;
import com.example.board.auth.mail.result.SendEmailResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailService {
    private final SpringTemplateEngine springTemplateEngine;
    private final EmailSender throttledEmailSender;

    public SendEmailResult sendEmail(MailContext mailContext) {
        String text;
        try {
            text = buildSignupAuthHtml(mailContext.otp(), mailContext.expiresInMinutes());
        } catch (Exception e) {
            // 재시도 X
            return new SendEmailResult.ComposeFailed(e);
        }
        try {
            throttledEmailSender.send(mailContext, text);
            return new SendEmailResult.Success();
        } catch (MailAuthenticationFailedException e) {
            return new SendEmailResult.AuthenticationFailed(e);
        } catch (MailComposeFailedException e) {
            return new SendEmailResult.ComposeFailed(e);
        } catch (MailSendFailedException e) {
            return new SendEmailResult.SendFailed(e);
        }
    }

    private String buildSignupAuthHtml(String otp, int expiresInMinutes) {
        var context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expiresInMinutes", expiresInMinutes);
        return springTemplateEngine.process("auth/mail/signup/auth.html", context);
    }
}
