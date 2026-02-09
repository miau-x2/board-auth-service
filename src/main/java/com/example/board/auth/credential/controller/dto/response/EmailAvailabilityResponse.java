package com.example.board.auth.credential.controller.dto.response;

public record EmailAvailabilityResponse(boolean available, String message) {
    public static EmailAvailabilityResponse available(String message) {
        return new EmailAvailabilityResponse(true, message);
    }
    public static EmailAvailabilityResponse unavailable(String message) {
        return new EmailAvailabilityResponse(false, message);
    }
}
