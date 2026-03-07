package com.example.board.auth.authentication.service;

import com.example.board.auth.authentication.service.command.IssueTokenCommand;
import com.example.board.auth.authentication.service.command.ReissueTokenCommand;
import com.example.board.auth.authentication.service.command.RevokeTokenCommand;
import com.example.board.auth.authentication.service.result.TokenPair;
import com.example.board.auth.authentication.token.ReissueTokensResult;

public interface TokenService {
    TokenPair issueTokens(IssueTokenCommand command);
    ReissueTokensResult reissueTokens(ReissueTokenCommand command);
    void revokeRefreshToken(RevokeTokenCommand command);
}
