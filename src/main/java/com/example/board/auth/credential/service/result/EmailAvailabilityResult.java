package com.example.board.auth.credential.service.result;

public sealed interface EmailAvailabilityResult {
    record Available(String message) implements EmailAvailabilityResult {}
    record Unavailable(String message) implements EmailAvailabilityResult {}
}
