package com.example.board.auth.authentication.service;

import com.example.board.auth.authentication.service.command.LoginCommand;
import com.example.board.auth.authentication.service.command.LogoutCommand;
import com.example.board.auth.authentication.service.command.ReissueCommand;
import com.example.board.auth.authentication.service.result.LoginResult;
import com.example.board.auth.authentication.service.result.ReissueResult;

public interface AuthService {
    LoginResult login(LoginCommand command);
    ReissueResult reissue(ReissueCommand command);
    void logout(LogoutCommand command);
}
