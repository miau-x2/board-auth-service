package com.example.board.auth.authentication.service.result;

import com.example.board.auth.authentication.token.impl.AccessToken;
import com.example.board.auth.authentication.token.impl.RefreshToken;

public record TokenPair(AccessToken accessToken, RefreshToken refreshToken) {
}
