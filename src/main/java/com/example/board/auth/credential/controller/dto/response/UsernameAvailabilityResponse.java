package com.example.board.auth.credential.controller.dto.response;

public record UsernameAvailabilityResponse(boolean available, String message) {
    public static UsernameAvailabilityResponse available(String message) {
        return new UsernameAvailabilityResponse(true, message);
    }
    public static UsernameAvailabilityResponse unavailable(String message) {
        return new UsernameAvailabilityResponse(false, message);
    }
}
