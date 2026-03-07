package com.example.board.auth.authentication.service.impl;

import com.example.board.auth.authentication.service.TokenService;
import com.example.board.auth.authentication.service.command.IssueTokenCommand;
import com.example.board.auth.authentication.service.command.ReissueTokenCommand;
import com.example.board.auth.authentication.service.command.RevokeTokenCommand;
import com.example.board.auth.authentication.service.result.TokenPair;
import com.example.board.auth.authentication.token.*;
import com.example.board.auth.authentication.token.impl.AccessToken;
import com.example.board.auth.authentication.token.impl.DefaultAuthTokenClaimsContext;
import com.example.board.auth.authentication.token.impl.DefaultAuthTokenContext;
import com.example.board.auth.authentication.token.impl.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final AuthTokenGenerator<AuthToken> authTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public TokenPair issueTokens(IssueTokenCommand command) {
        // 새로운 리프레시 토큰 생성
        var refreshToken = (RefreshToken) authTokenGenerator.generate(
                DefaultAuthTokenContext.builder()
                        .tokenType(AuthTokenType.REFRESH_TOKEN)
                        .subject(String.valueOf(command.memberId()))
                        .build()
        );
        // 리프레시 토큰 생성 실패
        if(refreshToken == null) {
            throw new IllegalStateException("Failed to generate refresh token");
        }
        // 리프레시 토큰 저장
        refreshTokenRepository.save(command.memberId(), refreshToken);
        // 새로운 액세스 토큰 생성
        var accessToken = (AccessToken) authTokenGenerator.generate(
                DefaultAuthTokenClaimsContext.builder()
                        .tokenType(AuthTokenType.ACCESS_TOKEN)
                        .subject(String.valueOf(command.memberId()))
                        .claims(Map.of("role", command.role().name()))
                        .build()
        );
        // 액세스 토큰 생성 실패
        if(accessToken == null) {
            // 리프레시 토큰 삭제
            refreshTokenRepository.remove(command.memberId(), refreshToken.getTokenValue());
            throw new IllegalStateException("Failed to generate access token");
        }

        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public ReissueTokensResult reissueTokens(ReissueTokenCommand command) {
        // 리프레시 토큰 생성
        var generatedRefreshToken = (RefreshToken) authTokenGenerator.generate(
                DefaultAuthTokenContext.builder()
                        .tokenType(AuthTokenType.REFRESH_TOKEN)
                        .subject(String.valueOf(command.memberId()))
                        .build()
        );
        if(generatedRefreshToken == null) {
            throw new IllegalStateException("Failed to generate refresh token");
        }
        var refreshTokenResult = refreshTokenRepository.rotateOrReplay(
                command.memberId(),
                command.refreshTokenValue(),
                generatedRefreshToken
        );

        return switch (refreshTokenResult) {
            case RotateOrReplayRefreshTokenResult.Success(var refreshToken) -> {
                // 액세스 토큰 생성
                var accessToken = (AccessToken) authTokenGenerator.generate(
                        DefaultAuthTokenClaimsContext.builder()
                                .tokenType(AuthTokenType.ACCESS_TOKEN)
                                .subject(String.valueOf(command.memberId()))
                                .claims(Map.of("role", command.role().name()))
                                .build()
                );
                // 액세스 토큰 생성 실패
                if(accessToken == null) {
                    throw new IllegalStateException("Failed to generate access token");
                }
                yield new ReissueTokensResult.Success(new TokenPair(accessToken, refreshToken));
            }
            case RotateOrReplayRefreshTokenResult.Invalid _ -> new ReissueTokensResult.InvalidRefreshToken();
        };
    }

    @Override
    public void revokeRefreshToken(RevokeTokenCommand command) {
        refreshTokenRepository.remove(command.memberId(), command.refreshTokenValue());
    }
}
