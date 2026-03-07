package com.example.board.auth.authentication.controller;

import com.example.board.auth.authentication.controller.dto.request.ReissueRequest;
import com.example.board.auth.authentication.controller.dto.response.ReissueResponse;
import com.example.board.auth.authentication.service.AuthService;
import com.example.board.auth.authentication.service.command.ReissueCommand;
import com.example.board.auth.authentication.service.result.ReissueResult;
import com.example.board.auth.commons.response.ApiResponse;
import com.example.board.auth.commons.response.AuthenticationErrorCode;
import com.example.board.auth.commons.response.AuthenticationSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.board.auth.commons.utils.ResponseUtils.errorResponse;
import static com.example.board.auth.commons.utils.ResponseUtils.successResponse;

@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
public class TokenController {
    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        var result = authService.reissue(new ReissueCommand(request.memberId(), request.refreshToken()));

        return switch (result) {
            case ReissueResult.Success(var authenticatedTokenPair) -> successResponse(
                    AuthenticationSuccessCode.TOKEN_REISSUED,
                    ReissueResponse.of(
                            authenticatedTokenPair.memberId(),
                            authenticatedTokenPair.role(),
                            authenticatedTokenPair.tokenPair().accessToken().getTokenValue(),
                            authenticatedTokenPair.tokenPair().accessToken().getExpiresAt(),
                            authenticatedTokenPair.tokenPair().refreshToken().getTokenValue(),
                            authenticatedTokenPair.tokenPair().refreshToken().getExpiresAt()
                    )
            );
            case ReissueResult.InvalidRefreshToken _ -> errorResponse(AuthenticationErrorCode.REFRESH_TOKEN_INVALID);
        };
    }
}
