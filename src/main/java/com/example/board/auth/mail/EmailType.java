package com.example.board.auth.mail;

public interface EmailType {
    String getSubject();
    String getDescription();
    boolean isHtml();
}
