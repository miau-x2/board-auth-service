package com.example.board.auth.credential.service.result;

public sealed interface UsernameAvailabilityResult {
    record Available(String message) implements UsernameAvailabilityResult {}
    record Unavailable(String message) implements UsernameAvailabilityResult {}
}
