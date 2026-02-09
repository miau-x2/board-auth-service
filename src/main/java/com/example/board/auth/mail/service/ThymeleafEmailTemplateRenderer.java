package com.example.board.auth.mail.service;

import com.example.board.auth.mail.AuthEmailType;
import com.example.board.auth.mail.EmailType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class ThymeleafEmailTemplateRenderer implements EmailTemplateRenderer {
    private final SpringTemplateEngine springTemplateEngine;

    @Override
    public String render(EmailType emailType, String otp, long expiresInMinutes) {
        var context = new Context();
        context.setVariable("subject", emailType.getSubject());
        context.setVariable("description", emailType.getDescription());
        context.setVariable("otp", otp);
        context.setVariable("expiresInMinutes", expiresInMinutes);
        return springTemplateEngine.process(AuthEmailType.TEMPLATE_SOURCE, context);
    }
}
