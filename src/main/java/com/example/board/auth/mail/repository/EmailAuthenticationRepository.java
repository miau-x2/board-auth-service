package com.example.board.auth.mail.repository;

import com.example.board.auth.mail.result.SaveOtpResult;

import java.util.function.Supplier;

public interface EmailAuthenticationRepository {
    SaveOtpResult.Signup saveSignupOtp(String email, Supplier<String> supplier);
    void saveSignupToken(String token, String email);
    String getOtp(String email);
    String useSignupToken(String token);
    void deleteOtp(String email);
}
