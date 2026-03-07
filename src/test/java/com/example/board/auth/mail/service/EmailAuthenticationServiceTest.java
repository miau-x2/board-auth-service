package com.example.board.auth.mail.service;

import com.example.board.auth.mail.AuthEmailType;
import com.example.board.auth.mail.event.EmailSendEvent;
import com.example.board.auth.mail.repository.EmailAuthenticationRepository;
import com.example.board.auth.mail.result.EmailAuthenticationResult;
import com.example.board.auth.mail.result.SaveOtpResult;
import com.example.board.auth.mail.service.command.EmailSendCommand;
import com.example.board.auth.mail.service.command.EmailVerifyCommand;
import com.example.board.auth.verification.token.OtpGenerator;
import com.example.board.auth.verification.token.SignupTokenGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailAuthenticationServiceTest {
    @Mock
    private OtpGenerator otpGenerator;
    @Mock
    private SignupTokenGenerator signupTokenGenerator;
    @Mock
    private EmailAuthenticationRepository emailAuthenticationRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmailAuthenticationService emailAuthenticationService;

    @Test
    @DisplayName("OTP 전송 - 성공")
    void should_ReturnSuccess_When_SendOtpRequestIsValid() {
        var emailType = AuthEmailType.SIGNUP;
        var email = "user@gmail.com";
        var command = new EmailSendCommand(email);
        var otp = "123456";
        when(emailAuthenticationRepository.saveSignupOtp(eq(email), any()))
                .thenReturn(new SaveOtpResult.Signup.Success(otp));

        var actual = emailAuthenticationService.sendOtp(emailType, command);
        assertThat(actual).isExactlyInstanceOf(EmailAuthenticationResult.SendOtp.Success.class);

        verify(emailAuthenticationRepository).saveSignupOtp(eq(email), any());
        verify(eventPublisher).publishEvent(any(EmailSendEvent.class));
    }

    @Test
    @DisplayName("OTP 전송 - 실패(지원하지 않는 이메일 도메인)")
    void should_ReturnEmailDomainNotAllowed_When_DomainIsNotSupported() {
        var emailType = AuthEmailType.SIGNUP;
        var command = new EmailSendCommand("user@unsupported.com");

        var actual = emailAuthenticationService.sendOtp(emailType, command);
        assertThat(actual).isExactlyInstanceOf(EmailAuthenticationResult.SendOtp.EmailDomainNotAllowed.class);

        verify(emailAuthenticationRepository, never()).saveSignupOtp(anyString(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("OTP 전송 - 실패(재전송 제한 시간 미경과)")
    void should_ReturnTooManyRequests_When_SetCooldownIsActive() {
        var emailType = AuthEmailType.SIGNUP;
        var email = "user@gmail.com";
        var command = new EmailSendCommand(email);
        var retryAfter = 30L;
        when(emailAuthenticationRepository.saveSignupOtp(eq(email), any()))
                .thenReturn(new SaveOtpResult.Signup.Cooldown(retryAfter));


        var actual = emailAuthenticationService.sendOtp(emailType, command);
        assertThat(actual).isExactlyInstanceOf(EmailAuthenticationResult.SendOtp.TooManyRequests.class)
                .satisfies(result -> {
                    var res = (EmailAuthenticationResult.SendOtp.TooManyRequests) result;
                    assertThat(res.retryAfterSeconds()).isEqualTo(retryAfter);
                });

        verify(emailAuthenticationRepository).saveSignupOtp(eq(email), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("OTP 검증 - 성공")
    void should_ReturnSuccess_When_OtpMatches() {
        // Given
        var email = "user@gmail.com";
        var otp = "123456";
        var command = new EmailVerifyCommand(email, otp);
        var signupToken = "generated-token";
        when(emailAuthenticationRepository.getOtp(email)).thenReturn(otp);
        when(signupTokenGenerator.generate()).thenReturn(signupToken);

        var actual = emailAuthenticationService.verifyOtp(command);
        assertThat(actual).isExactlyInstanceOf(EmailAuthenticationResult.VerifyOtp.Success.class)
                .satisfies(result -> {
                    var res = (EmailAuthenticationResult.VerifyOtp.Success) result;
                    assertThat(res.token()).isEqualTo(signupToken);
                });

        verify(emailAuthenticationRepository).getOtp(email);
        verify(emailAuthenticationRepository).deleteOtp(email);
        verify(signupTokenGenerator).generate();
        verify(emailAuthenticationRepository).saveSignupToken(signupToken, email);
    }

    @Test
    @DisplayName("OTP 검증 - 실패(만료되었거나 존재하지 않음)")
    void should_ReturnExpired_When_OtpIsMissingOrExpired() {
        var email = "user@gmail.com";
        var command = new EmailVerifyCommand(email, "123456");
        when(emailAuthenticationRepository.getOtp(email)).thenReturn(null);

        var actual = emailAuthenticationService.verifyOtp(command);
        assertThat(actual).isExactlyInstanceOf(EmailAuthenticationResult.VerifyOtp.Expired.class);

        verify(emailAuthenticationRepository).getOtp(email);
        verify(emailAuthenticationRepository, never()).deleteOtp(anyString());
        verify(signupTokenGenerator, never()).generate();
    }

    @Test
    @DisplayName("OTP 검증 - 실패(OTP 불일치)")
    void should_ReturnInvalid_When_OtpDoesNotMatch() {
        var email = "user@gmail.com";
        var command = new EmailVerifyCommand(email, "wrong-otp");
        when(emailAuthenticationRepository.getOtp(email)).thenReturn("correct-otp");

        var actual = emailAuthenticationService.verifyOtp(command);
        assertThat(actual).isExactlyInstanceOf(EmailAuthenticationResult.VerifyOtp.Invalid.class);

        verify(emailAuthenticationRepository).getOtp(email);
        verify(emailAuthenticationRepository, never()).deleteOtp(anyString());
        verify(signupTokenGenerator, never()).generate();
    }
}
