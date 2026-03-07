package com.example.board.auth.authentication.controller;

import com.example.board.auth.authentication.controller.dto.request.LoginRequest;
import com.example.board.auth.authentication.controller.dto.request.LogoutRequest;
import com.example.board.auth.authentication.controller.dto.response.LoginResponse;
import com.example.board.auth.authentication.service.AuthService;
import com.example.board.auth.authentication.service.command.LoginCommand;
import com.example.board.auth.authentication.service.command.LogoutCommand;
import com.example.board.auth.authentication.service.result.LoginResult;
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
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        var result = authService.login(new LoginCommand(request.username(), request.password()));

        return switch (result) {
            case LoginResult.Success(var authenticatedTokenPair) -> successResponse(
                    AuthenticationSuccessCode.LOGIN_SUCCESS,
                    LoginResponse.of(
                            authenticatedTokenPair.memberId(),
                            authenticatedTokenPair.role(),
                            authenticatedTokenPair.tokenPair().accessToken().getTokenValue(),
                            authenticatedTokenPair.tokenPair().accessToken().getExpiresAt(),
                            authenticatedTokenPair.tokenPair().refreshToken().getTokenValue(),
                            authenticatedTokenPair.tokenPair().refreshToken().getExpiresAt()
                    )
            );
            case LoginResult.BadCredentials _ -> errorResponse(AuthenticationErrorCode.BAD_CREDENTIALS);
            case LoginResult.AccountPending _ -> errorResponse(AuthenticationErrorCode.ACCOUNT_PENDING);
            case LoginResult.AccountDormant _ -> errorResponse(AuthenticationErrorCode.ACCOUNT_DORMANT);
            case LoginResult.AccountWithdrawn _ -> errorResponse(AuthenticationErrorCode.ACCOUNT_WITHDRAWN);
        };
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(new LogoutCommand(request.memberId(), request.refreshToken()));
        return successResponse(AuthenticationSuccessCode.LOGOUT_SUCCESS);
    }
}
