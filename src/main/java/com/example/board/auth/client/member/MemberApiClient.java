package com.example.board.auth.client.member;

import com.example.board.auth.commons.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member-service", contextId = "memberApiClient")
public interface MemberApiClient {
    @PutMapping("/{member-id}/profile")
    ApiResponse<Void> createProfile(@PathVariable("member-id") Long id, @Valid @RequestBody MemberProfileCreateRequest request);

    @DeleteMapping("/{member-id}/profile")
    ApiResponse<Void> softDeleteProfile(@PathVariable("member-id") Long id);

    @DeleteMapping("/internal/{member-id}/profile")
    ApiResponse<Void> hardDeleteProfile(@PathVariable("member-id") Long id);
}
