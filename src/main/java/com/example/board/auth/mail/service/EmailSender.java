package com.example.board.auth.mail.service;

import com.example.board.auth.mail.dto.MailContext;

public interface EmailSender {
    void send(MailContext mailContext, String text);
}
