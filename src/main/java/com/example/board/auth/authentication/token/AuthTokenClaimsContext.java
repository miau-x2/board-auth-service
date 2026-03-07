package com.example.board.auth.authentication.token;

import java.util.Map;

public interface AuthTokenClaimsContext extends AuthTokenContext {
    Map<String, Object> getClaims();
}
