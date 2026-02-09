package com.example.board.auth.credential.controller.dto.response;

public record NicknameAvailabilityResponse(boolean available, String message) {
    public static NicknameAvailabilityResponse available(String message) {
        return new NicknameAvailabilityResponse(true, message);
    }
    public static NicknameAvailabilityResponse unavailable(String message) {
        return new NicknameAvailabilityResponse(false, message);
    }
}
