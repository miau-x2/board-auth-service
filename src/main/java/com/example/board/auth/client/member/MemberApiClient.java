package com.example.board.auth.client.member;

import com.example.board.auth.commons.response.ApiResponse;
import com.example.board.auth.credential.controller.dto.response.NicknameAvailabilityResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "member-service", contextId = "memberApiClient")
public interface MemberApiClient {
    @PutMapping("/api/members/{member-id}/profile")
    ApiResponse<Void> createProfile(@PathVariable("member-id") Long id, @Valid @RequestBody MemberProfileCreateRequest request);

    @DeleteMapping("/api/members/{member-id}/profile")
    ApiResponse<Void> softDeleteProfile(@PathVariable("member-id") Long id);

    @DeleteMapping("/api/members/internal/{member-id}/profile")
    ApiResponse<Void> hardDeleteProfile(@PathVariable("member-id") Long id);

    @GetMapping("/api/members/check-nickname")
    ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(@RequestParam String nickname);
}
