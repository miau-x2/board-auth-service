package com.example.board.auth.credential.controller;

import com.example.board.auth.credential.controller.dto.request.MemberSignupRequest;
import com.example.board.auth.credential.controller.dto.validation.SignupValidationSequence;
import com.example.board.auth.credential.orchestrator.MemberSignupOrchestrator;
import com.example.board.auth.credential.service.command.MemberSignupCommand;
import com.example.board.auth.credential.service.result.SignupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberSignupController {
    private final MemberSignupOrchestrator memberSignupOrchestrator;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupFormData", MemberSignupRequest.empty());
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(
            @CookieValue(value = "reg_tkt", required = false) String token,
            @Validated(SignupValidationSequence.class)
            @ModelAttribute("signupFormData")
            MemberSignupRequest request,
            BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "member/signup";
        }

        var result = memberSignupOrchestrator.coordinateSignup(new MemberSignupCommand(request.username(), request.password(), request.email(), request.nickname(), token));

        return switch (result) {
            case SignupResult.Success _ -> "redirect:http://localhost:8000/";
            case SignupResult.EmailDomainNotAllowed _ ->
                    signupFieldError(bindingResult, "email", "email.domainNotAllowed", "지메일과 네이버메일만 사용할 수 있습니다.");
            case SignupResult.EmailAlreadyExists _ ->
                    signupFieldError(bindingResult, "email", "email.duplicate", "이미 사용 중인 이메일입니다.");
            case SignupResult.UsernameAlreadyExists _ ->
                    signupFieldError(bindingResult, "username", "username.duplicate", "이미 사용 중인 아이디입니다.");
            case SignupResult.NicknameAlreadyExists _ ->
                    signupFieldError(bindingResult, "nickname", "nickname.duplicate", "이미 사용 중인 닉네임입니다.");

            case SignupResult.TokenExpired _ ->
                    signupGlobalError(bindingResult, "token.expired", "이메일 인증이 만료되었습니다. 다시 인증해주세요.");
            case SignupResult.TokenInvalid _ ->
                    signupGlobalError(bindingResult, "token.invalid", "이메일 인증이 유효하지 않습니다. 다시 인증해주세요.");
            case SignupResult.UnexpectedValidationError _,
                 SignupResult.UnexpectedConflictError _,
                 SignupResult.DownstreamServiceError _,
                 SignupResult.SystemError _ ->
                    signupGlobalError(bindingResult, "signup.system", "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
        };
    }

    private void addFieldError(BindingResult bindingResult, String field, String errorCode, String defaultMessage) {
        bindingResult.rejectValue(field, errorCode, defaultMessage);
    }

    private void addGlobalError(BindingResult bindingResult, String errorCode, String defaultMessage) {
        bindingResult.reject(errorCode, defaultMessage);
    }

    private String signupFieldError(BindingResult bindingResult, String field, String errorCode, String defaultMessage) {
        addFieldError(bindingResult, field, errorCode, defaultMessage);
        return "member/signup";
    }

    private String signupGlobalError(BindingResult bindingResult, String errorCode, String defaultMessage) {
        addGlobalError(bindingResult, errorCode, defaultMessage);
        return "member/signup";
    }
}
