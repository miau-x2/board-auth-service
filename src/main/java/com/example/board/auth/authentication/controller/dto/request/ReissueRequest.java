package com.example.board.auth.authentication.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReissueRequest(
        @NotNull
        Long memberId,
        @NotBlank
        String refreshToken
)
{}
