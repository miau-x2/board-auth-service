package com.example.board.auth.credential.controller;

import com.example.board.auth.client.member.MemberApiClient;
import com.example.board.auth.commons.response.ApiResponse;
import com.example.board.auth.commons.response.EmailAuthenticationErrorCode;
import com.example.board.auth.commons.response.EmailAuthenticationSuccessCode;
import com.example.board.auth.commons.response.MemberCredentialSuccessCode;
import com.example.board.auth.credential.controller.dto.request.EmailSendRequest;
import com.example.board.auth.credential.controller.dto.request.EmailVerifyRequest;
import com.example.board.auth.credential.controller.dto.response.EmailAvailabilityResponse;
import com.example.board.auth.credential.controller.dto.response.NicknameAvailabilityResponse;
import com.example.board.auth.credential.controller.dto.response.SignupEmailSendResponse;
import com.example.board.auth.credential.controller.dto.response.UsernameAvailabilityResponse;
import com.example.board.auth.credential.service.MemberService;
import com.example.board.auth.credential.service.result.EmailAvailabilityResult;
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
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.board.auth.commons.utils.ResponseUtils.errorResponse;
import static com.example.board.auth.commons.utils.ResponseUtils.successResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberSignupRestController {
    private final MemberService memberService;
    private final MemberApiClient memberApiClient;
    private final EmailAuthenticationService emailAuthenticationService;

    @GetMapping("/members/check-username")
    public ResponseEntity<ApiResponse<UsernameAvailabilityResponse>> checkUsername(
            @NotBlank(message = "아이디를 입력해주세요.")
            @Size(min = 5, max = 20, message = "아이디는 5~20자입니다.")
            @Pattern(
                    regexp = "^(?=.*[a-z])[a-z0-9]+$",
                    message = "아이디는 영문 소문자와 숫자만 가능하며 영문은 필수입니다."
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
    public ResponseEntity<ApiResponse<EmailAvailabilityResponse>> checkEmail(
            @NotBlank(message = "이메일을 입력해주세요.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @RequestParam
            String email) {
        var result = memberService.checkEmailAvailability(email);

        return switch (result) {
            case EmailAvailabilityResult.Available(var message) ->
                    successResponse(MemberCredentialSuccessCode.EMAIL_AVAILABILITY_CHECKED,EmailAvailabilityResponse.available(message));
            case EmailAvailabilityResult.UnAvailable(var message) ->
                    successResponse(MemberCredentialSuccessCode.EMAIL_AVAILABILITY_CHECKED, EmailAvailabilityResponse.unavailable(message));
        };
    }

    @GetMapping("/members/check-nickname")
    public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNickname(
            @NotBlank(message = "닉네임을 입력해주세요.")
            @Size(min = 2, max = 20, message = "닉네임은 2~20자입니다.")
            @Pattern(
                    regexp = "^[a-z0-9가-힣]+$",
                    message = "닉네임은 한글, 영문 소문자, 숫자만 사용할 수 있습니다."
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
                    successResponse(EmailAuthenticationSuccessCode.OTP_SENT,new SignupEmailSendResponse(otpValiditySeconds, cooldownSeconds));
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
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody EmailVerifyRequest request) {
        var result = emailAuthenticationService.verifyOtp(new EmailVerifyCommand(request.email(), request.otp()));

        return switch (result) {
            case EmailAuthenticationResult.VerifyOtp.Success(var token) -> {
                var cookie = ResponseCookie
                        .from("reg_tkt", token)
                        .httpOnly(true)
                        .secure(false)
                        .path("/auth")
                        .sameSite("Lax")
                        .build();
                var code = EmailAuthenticationSuccessCode.EMAIL_VERIFIED;
                yield ResponseEntity
                        .status(code.getHttpStatus())
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(ApiResponse.success(code));
            }
            case EmailAuthenticationResult.VerifyOtp.Expired _ ->
                    errorResponse(EmailAuthenticationErrorCode.OTP_EXPIRED);
            case EmailAuthenticationResult.VerifyOtp.Invalid _ ->
                    errorResponse(EmailAuthenticationErrorCode.OTP_INVALID);
        };
    }
}
