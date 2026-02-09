package com.example.board.auth.mail.dto;

public record MailContext(String to, String subject, String text, boolean hasHtml) {
}
