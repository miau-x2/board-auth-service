package com.example.board.auth.authentication.service.impl;

import com.example.board.auth.authentication.security.CustomUserDetails;
import com.example.board.auth.authentication.security.exception.AccountDormantAuthenticationException;
import com.example.board.auth.authentication.security.exception.AccountPendingAuthenticationException;
import com.example.board.auth.authentication.security.exception.AccountWithdrawnAuthenticationException;
import com.example.board.auth.authentication.service.AuthService;
import com.example.board.auth.authentication.service.TokenService;
import com.example.board.auth.authentication.service.command.*;
import com.example.board.auth.authentication.service.result.AuthenticatedTokenPair;
import com.example.board.auth.authentication.service.result.LoginResult;
import com.example.board.auth.authentication.service.result.ReissueResult;
import com.example.board.auth.authentication.token.ReissueTokensResult;
import com.example.board.auth.credential.service.MemberService;
import com.example.board.auth.credential.service.result.GetCredentialResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final MemberService memberService;

    @Override
    public LoginResult login(LoginCommand command) {
        try {
            var authRequest = UsernamePasswordAuthenticationToken.unauthenticated(command.username(), command.password());
            var authenticated = authenticationManager.authenticate(authRequest);
            if(!(authenticated.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
                throw new IllegalStateException("Principal must be CustomUserDetails");
            }

            var tokenPair = tokenService.issueTokens(new IssueTokenCommand(customUserDetails.getId(), customUserDetails.getRole()));
            log.info("로그인 성공: {}", command.username());
            updateLastLogin(customUserDetails.getId());
            return new LoginResult.Success(new AuthenticatedTokenPair(customUserDetails.getId(), customUserDetails.getRole().name(), tokenPair));
        } catch (BadCredentialsException _) {
            log.warn("로그인 실패. 아이디 또는 비밀번호가 일치하지 않습니다.");
            return new LoginResult.BadCredentials();
        } catch (AccountDormantAuthenticationException _) {
            log.warn("로그인 실패. 휴면 계정입니다.");
            return new LoginResult.AccountDormant();
        } catch (AccountPendingAuthenticationException _) {
            log.warn("로그인 실패. 유효하지 않은 계정입니다.");
            return new LoginResult.AccountPending();
        } catch (AccountWithdrawnAuthenticationException _) {
            log.warn("로그인 실패. 탈퇴한 계정입니다.");
            return new LoginResult.AccountWithdrawn();
        }
    }

    @Override
    public ReissueResult reissue(ReissueCommand command) {
        var roleResult = memberService.getMemberRole(command.memberId());
        return switch (roleResult) {
            case GetCredentialResult.Role.NotFound _ -> new ReissueResult.InvalidRefreshToken();
            case GetCredentialResult.Role.Success(var role) -> processReissueTokens(new ReissueTokenCommand(command.memberId(), command.refreshToken(), role));
        };
    }

    @Override
    public void logout(LogoutCommand command) {
        tokenService.revokeRefreshToken(new RevokeTokenCommand(command.memberId(), command.refreshToken()));
    }

    private ReissueResult processReissueTokens(ReissueTokenCommand command) {
        var tokenResult = tokenService.reissueTokens(command);
        return switch (tokenResult) {
            case ReissueTokensResult.Success(var tokenPair) -> new ReissueResult.Success(new AuthenticatedTokenPair(command.memberId(), command.role().name(), tokenPair));
            case ReissueTokensResult.InvalidRefreshToken _ -> new ReissueResult.InvalidRefreshToken();
        };
    }

    private void updateLastLogin(Long id) {
        try {
            memberService.updateLastLogin(id);
        } catch (Exception e) {
            log.warn("회원: {}의 최종 로그인 시간 업데이트 실패: {}", id, e.getMessage(), e);
        }
    }
}
