package com.example.board.auth.authentication.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogoutRequest(
        @NotNull
        Long memberId,
        @NotBlank
        String refreshToken
)
{}
