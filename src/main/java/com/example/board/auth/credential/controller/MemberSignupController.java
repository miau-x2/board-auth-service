package com.example.board.auth.credential.controller;

import com.example.board.auth.annotation.SignupToken;
import com.example.board.auth.client.member.MemberApiClient;
import com.example.board.auth.commons.response.*;
import com.example.board.auth.credential.controller.dto.request.EmailSendRequest;
import com.example.board.auth.credential.controller.dto.request.EmailVerifyRequest;
import com.example.board.auth.credential.controller.dto.request.MemberSignupRequest;
import com.example.board.auth.credential.controller.dto.validation.FormatGroup;
import com.example.board.auth.credential.controller.dto.validation.NotBlankGroup;
import com.example.board.auth.credential.controller.dto.validation.SignupValidationSequence;
import com.example.board.auth.credential.controller.dto.validation.SizeGroup;
import com.example.board.auth.credential.controller.dto.response.EmailAvailabilityResponse;
import com.example.board.auth.credential.controller.dto.response.NicknameAvailabilityResponse;
import com.example.board.auth.credential.controller.dto.response.SignupEmailSendResponse;
import com.example.board.auth.credential.controller.dto.response.SignupEmailVerifyResponse;
import com.example.board.auth.credential.controller.dto.response.UsernameAvailabilityResponse;
import com.example.board.auth.credential.orchestrator.MemberSignupOrchestrator;
import com.example.board.auth.credential.service.MemberService;
import com.example.board.auth.credential.service.command.MemberSignupCommand;
import com.example.board.auth.credential.service.result.EmailAvailabilityResult;
import com.example.board.auth.credential.service.result.SignupResult;
import com.example.board.auth.credential.service.result.UsernameAvailabilityResult;
import com.example.board.auth.mail.AuthEmailType;
import com.example.board.auth.mail.result.EmailAuthenticationResult;
import com.example.board.auth.mail.service.EmailAuthenticationService;
import com.example.board.auth.mail.service.command.EmailSendCommand;
import com.example.board.auth.mail.service.command.EmailVerifyCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.example.board.auth.commons.utils.ResponseUtils.errorResponse;
import static com.example.board.auth.commons.utils.ResponseUtils.successResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class MemberSignupController {
    private final MemberSignupOrchestrator memberSignupOrchestrator;
    private final MemberService memberService;
    private final MemberApiClient memberApiClient;
    private final EmailAuthenticationService emailAuthenticationService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@SignupToken String token, @Valid @RequestBody MemberSignupRequest request) {
        var result = memberSignupOrchestrator.coordinateSignup(new MemberSignupCommand(request.username(), request.password(), request.email(), request.nickname(), token));

        return switch (result) {
            case SignupResult.Success _ -> successResponse(MemberCredentialSuccessCode.CREDENTIAL_CREATED);
            case SignupResult.TokenExpired _ -> errorResponse(MemberCredentialErrorCode.TOKEN_EXPIRED);
            case SignupResult.TokenInvalid _ -> errorResponse(MemberCredentialErrorCode.TOKEN_INVALID);
            case SignupResult.EmailDomainNotAllowed _ -> errorResponse(MemberCredentialErrorCode.EMAIL_DOMAIN_NOT_ALLOWED);
            case SignupResult.EmailAlreadyExists _ -> errorResponse(MemberCredentialErrorCode.EMAIL_DUPLICATED);
            case SignupResult.UsernameAlreadyExists _ -> errorResponse(MemberCredentialErrorCode.USERNAME_DUPLICATED);
            case SignupResult.NicknameAlreadyExists _ -> errorResponse(MemberCredentialErrorCode.NICKNAME_DUPLICATED);
            case SignupResult.UnexpectedValidationError _,
                 SignupResult.UnexpectedConflictError _,
                 SignupResult.DownstreamServiceError _,
                 SignupResult.SystemError _ -> errorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
        };
    }

    @GetMapping("/members/check-username")
    @Validated(SignupValidationSequence.class)
    public ResponseEntity<ApiResponse<UsernameAvailabilityResponse>> checkUsername(
            @NotBlank(message = "아이디를 입력해주세요.", groups = NotBlankGroup.class)
            @Size(min = 5, max = 20, message = "아이디는 5~20자입니다.", groups = SizeGroup.class)
            @Pattern(
                    regexp = "^(?=.*[a-z])[a-z0-9]+$",
                    message = "아이디는 영문 소문자와 숫자만 가능하며 영문은 필수입니다.",
                    groups = FormatGroup.class
            )
            @RequestParam
            String username) {
        var result = memberService.checkUsernameAvailability(username);

        return switch (result) {
            case UsernameAvailabilityResult.Available(var message) ->
                    successResponse(MemberCredentialSuccessCode.USERNAME_AVAILABILITY_CHECKED, UsernameAvailabilityResponse.available(message));
            case UsernameAvailabilityResult.Unavailable(var message) ->
                    successResponse(MemberCredentialSuccessCode.USERNAME_AVAILABILITY_CHECKED, UsernameAvailabilityResponse.unavailable(message));
        };
    }

    @GetMapping("/members/check-email")
    @Validated(SignupValidationSequence.class)
    public ResponseEntity<ApiResponse<EmailAvailabilityResponse>> checkEmail(
            @NotBlank(message = "이메일을 입력해주세요.", groups = NotBlankGroup.class)
            @Email(message = "이메일 형식이 올바르지 않습니다.", groups = FormatGroup.class)
            @RequestParam
            String email) {
        var result = memberService.checkEmailAvailability(email);

        return switch (result) {
            case EmailAvailabilityResult.Available(var message) ->
                    successResponse(MemberCredentialSuccessCode.EMAIL_AVAILABILITY_CHECKED,EmailAvailabilityResponse.available(message));
            case EmailAvailabilityResult.Unavailable(var message) ->
                    successResponse(MemberCredentialSuccessCode.EMAIL_AVAILABILITY_CHECKED, EmailAvailabilityResponse.unavailable(message));
        };
    }

    @GetMapping("/members/check-nickname")
    @Validated(SignupValidationSequence.class)
    public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNickname(
            @NotBlank(message = "닉네임을 입력해주세요.", groups = NotBlankGroup.class)
            @Size(min = 2, max = 20, message = "닉네임은 2~20자입니다.", groups = SizeGroup.class)
            @Pattern(
                    regexp = "^[a-z0-9가-힣]+$",
                    message = "닉네임은 한글, 영문 소문자, 숫자만 사용할 수 있습니다.",
                    groups = FormatGroup.class
            )
            @RequestParam
            String nickname) {
        return memberApiClient.checkNicknameAvailability(nickname);
    }

    @PostMapping("/signup/email-verification")
    public ResponseEntity<ApiResponse<SignupEmailSendResponse>> issueOtpEmail(@Valid @RequestBody EmailSendRequest request) {
        var result = emailAuthenticationService.sendOtp(AuthEmailType.SIGNUP, new EmailSendCommand(request.email()));

        return switch (result) {
            case EmailAuthenticationResult.SendOtp.Success(var otpValiditySeconds, var cooldownSeconds) ->
                    successResponse(EmailAuthenticationSuccessCode.OTP_SENT, new SignupEmailSendResponse(otpValiditySeconds, cooldownSeconds));
            case EmailAuthenticationResult.SendOtp.EmailDomainNotAllowed _ ->
                    errorResponse(EmailAuthenticationErrorCode.EMAIL_DOMAIN_NOT_ALLOWED);
            case EmailAuthenticationResult.SendOtp.TooManyRequests(var retryAfterSeconds) -> {
                var code = EmailAuthenticationErrorCode.TOO_MANY_REQUESTS;
                yield ResponseEntity
                        .status(code.getHttpStatus())
                        .header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds))
                        .body(ApiResponse.error(code));
            }
        };
    }

    @PostMapping("/signup/email-verification/verify")
    public ResponseEntity<ApiResponse<SignupEmailVerifyResponse>> verifyOtp(@Valid @RequestBody EmailVerifyRequest request) {
        var result = emailAuthenticationService.verifyOtp(new EmailVerifyCommand(request.email(), request.otp()));

        return switch (result) {
            case EmailAuthenticationResult.VerifyOtp.Success(var token) ->
                    successResponse(EmailAuthenticationSuccessCode.EMAIL_VERIFIED, new SignupEmailVerifyResponse(token));
            case EmailAuthenticationResult.VerifyOtp.Expired _ ->
                    errorResponse(EmailAuthenticationErrorCode.OTP_EXPIRED);
            case EmailAuthenticationResult.VerifyOtp.Invalid _ ->
                    errorResponse(EmailAuthenticationErrorCode.OTP_INVALID);
        };
    }
}
