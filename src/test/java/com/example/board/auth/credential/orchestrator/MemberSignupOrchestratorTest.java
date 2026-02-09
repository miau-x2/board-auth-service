package com.example.board.auth.credential.orchestrator;

import com.example.board.auth.client.exception.FeignExceptions;
import com.example.board.auth.client.member.MemberApiClient;
import com.example.board.auth.client.member.MemberProfileCreateRequest;
import com.example.board.auth.commons.exception.RetryableRemoteException;
import com.example.board.auth.commons.response.ApiResponse;
import com.example.board.auth.credential.exception.MemberCredentialCompensationFailedException;
import com.example.board.auth.credential.exception.MemberProfileCompensationFailedException;
import com.example.board.auth.credential.service.MemberService;
import com.example.board.auth.credential.service.command.MemberCredentialCreateCommand;
import com.example.board.auth.credential.service.command.MemberSignupCommand;
import com.example.board.auth.credential.service.result.ActivateCredentialResult;
import com.example.board.auth.credential.service.result.CreateCredentialResult;
import com.example.board.auth.credential.service.result.SignupResult;
import com.example.board.auth.credential.tx.MemberCredentialTxWriter;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberSignupOrchestratorTest {
    @Mock
    private MemberApiClient memberApiClient;
    @Mock
    private MemberService memberService;
    @Mock
    private FeignExceptions feignExceptions;
    @Mock
    private MemberCredentialTxWriter memberCredentialTxWriter;
    private MemberSignupOrchestrator memberSignupOrchestrator;

    @BeforeEach
    void setUp() {
        var retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ZERO)
                .includes(RetryableRemoteException.class)
                .build();
        var retryTemplate = new RetryTemplate(retryPolicy);
        memberSignupOrchestrator = new MemberSignupOrchestrator(memberApiClient, memberService, retryTemplate, feignExceptions, memberCredentialTxWriter);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 성공")
    void should_ReturnSuccess_When_SignupProcessSucceeds() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberService.activateCredential(id)).thenReturn(new ActivateCredentialResult.Success());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.Success.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient).createProfile(eq(id), any(MemberProfileCreateRequest.class));
        verify(memberService).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(지원하지 않는 이메일 도메인)")
    void should_ReturnEmailDomainNotAllowed_When_DomainIsNotSupported() {
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.EmailDomainNotAllowed());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.EmailDomainNotAllowed.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberService, never()).activateCredential(anyLong());
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(이메일 인증 토큰 만료)")
    void should_ReturnTokenExpired_When_EmailAuthTokenIsExpired() {
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.TokenExpired());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.TokenExpired.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberService, never()).activateCredential(anyLong());
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(유효 하지 않은 이메일 토큰)")
    void should_ReturnTokenInvalid_When_EmailAuthTokenIsInvalid() {
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.TokenInvalid());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.TokenInvalid.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberService, never()).activateCredential(anyLong());
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(이메일 중복)")
    void should_ReturnEmailAlreadyExists_When_EmailIsDuplicated() {
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.EmailAlreadyExists());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.EmailAlreadyExists.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberService, never()).activateCredential(anyLong());
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(아이디 중복)")
    void should_ReturnUsernameAlreadyExists_When_UsernameIsDuplicated() {
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.UsernameAlreadyExists());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.UsernameAlreadyExists.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberService, never()).activateCredential(anyLong());
    }

    // 400
    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(입력값 검증 정책 상이)")
    void should_ReturnUnexpectedValidationError_When_CreateProfileApiReturnBadRequest() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        var badRequest = new FeignException.BadRequest("msg", createProfileRequest(), null, null);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest)).thenThrow(badRequest);

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.UnexpectedValidationError.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient).createProfile(id, memberProfileRequest);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberService, never()).activateCredential(id);
    }

    // 409
    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(핸들 중복)")
    void should_ReturnUsernameAlreadyExists_When_HandleIsDuplicated() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        var conflict = new FeignException.Conflict("msg", createProfileRequest(), null, null);
        var apiResponse = new ApiResponse<Void>(false, "MEMBER_PROFILE_409_001", "msg", null);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest)).thenThrow(conflict);
        when(feignExceptions.extractErrorResponse(conflict)).thenReturn(Optional.of(apiResponse));

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.UsernameAlreadyExists.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient).createProfile(id, memberProfileRequest);
        verify(feignExceptions).extractErrorResponse(conflict);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(닉네임 중복)")
    void should_ReturnNicknameAlreadyExists_When_NicknameIsDuplicated() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        var conflict = new FeignException.Conflict("msg", createProfileRequest(), null, null);
        var apiResponse = new ApiResponse<Void>(false, "MEMBER_PROFILE_409_002", "msg", null);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest)).thenThrow(conflict);
        when(feignExceptions.extractErrorResponse(conflict)).thenReturn(Optional.of(apiResponse));

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.NicknameAlreadyExists.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient).createProfile(id, memberProfileRequest);
        verify(feignExceptions).extractErrorResponse(conflict);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(정의되지 않은 409 응답)")
    void should_ReturnUnexpectedConflictError_When_CreateProfileApiReturnUnexpectedConflict() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        var conflict = new FeignException.Conflict("msg", createProfileRequest(), null, null);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest)).thenThrow(conflict);
        when(feignExceptions.extractErrorResponse(conflict)).thenReturn(Optional.empty());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.UnexpectedConflictError.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient).createProfile(id, memberProfileRequest);
        verify(feignExceptions).extractErrorResponse(conflict);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberService, never()).activateCredential(id);
    }

    // 500
    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(재시도 할 수 없는 예외)")
    void should_ReturnDownstreamServiceError_When_CreateProfileApiReturnNonRetryableError() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        var internalServerError = new FeignException.InternalServerError("msg", createProfileRequest(), null, null);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest)).thenThrow(internalServerError);
        when(feignExceptions.isRetryableStatus(anyInt())).thenReturn(false);

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.DownstreamServiceError.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient).createProfile(id, memberProfileRequest);
        verify(feignExceptions).isRetryableStatus(anyInt());
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 성공(재시도 할 수 있는 예외)")
    void should_ReturnSuccess_When_CreateProfileApiReturnRetryableError() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        var serviceUnavailable = new FeignException.ServiceUnavailable("msg", createProfileRequest(), null, null);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest))
                .thenThrow(serviceUnavailable)
                .thenThrow(serviceUnavailable)
                .thenReturn(new ApiResponse<>(true, "code", "msg", null));
        when(feignExceptions.isRetryableStatus(anyInt())).thenReturn(true);
        when(memberService.activateCredential(id)).thenReturn(new ActivateCredentialResult.Success());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.Success.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient, times(3)).createProfile(id, memberProfileRequest);
        verify(feignExceptions, times(2)).isRetryableStatus(anyInt());
        verify(memberService).activateCredential(id);
        verify(memberCredentialTxWriter, never()).hardDeleteCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(재시도 할 수 있는 예외)")
    void should_ReturnDownstreamServiceError_When_CreateProfileApiExhaustsRetries() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        var serviceUnavailable = new FeignException.ServiceUnavailable("msg", createProfileRequest(), null, null);
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest)).thenThrow(serviceUnavailable);
        when(feignExceptions.isRetryableStatus(anyInt())).thenReturn(true);

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.DownstreamServiceError.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient, times(3)).createProfile(id, memberProfileRequest);
        verify(feignExceptions, times(3)).isRetryableStatus(anyInt());
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(자격 증명 활성화 실패 - NotFound)")
    void should_ReturnSystemError_When_ActivateCredentialReturnNotFound() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest))
                .thenReturn(new ApiResponse<>(true, "code", "msg", null));
        when(memberService.activateCredential(id)).thenReturn(new ActivateCredentialResult.NotFound());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.SystemError.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient).createProfile(id, memberProfileRequest);
        verify(memberService).activateCredential(id);
        verify(memberApiClient).hardDeleteProfile(id);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(자격 증명 활성화 실패 - Failure)")
    void should_ReturnSystemError_When_ActivateCredentialReturnFailure() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest))
                .thenReturn(new ApiResponse<>(true, "code", "msg", null));
        when(memberService.activateCredential(id)).thenReturn(new ActivateCredentialResult.Failure());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);
        assertThat(actual)
                .isExactlyInstanceOf(SignupResult.SystemError.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient).createProfile(id, memberProfileRequest);
        verify(memberService).activateCredential(id);
        verify(memberApiClient).hardDeleteProfile(id);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(자격 증명 활성화 실패 - 보상 트랜잭션 프로필 삭제 실패)")
    void should_ThrowMemberProfileCompensationFailedException_When_ProfileDeletionFails() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest)).thenReturn(new ApiResponse<>(true, "code", "msg", null));
        when(memberService.activateCredential(id)).thenReturn(new ActivateCredentialResult.Failure());

        var internalServerError = new FeignException.InternalServerError("msg", createProfileRequest(), null, null);
        when(memberApiClient.hardDeleteProfile(id)).thenThrow(internalServerError);
        when(feignExceptions.isRetryableStatus(anyInt())).thenReturn(false);

        assertThatThrownBy(() -> memberSignupOrchestrator.coordinateSignup(signupCommand))
                .isExactlyInstanceOf(MemberProfileCompensationFailedException.class);

        verify(memberService).createCredential(credentialCreateCommand);
        verify(memberApiClient).createProfile(id, memberProfileRequest);
        verify(memberService).activateCredential(id);
        verify(memberApiClient).hardDeleteProfile(id);
        verify(memberCredentialTxWriter, never()).hardDeleteCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이터 - 실패(보상 트랜잭션 자격 증명 삭제 실패)")
    void should_ThrowMemberCredentialCompensationFailedException_When_CredentialDeletionFails() {
        var id = 1L;
        var username = "test-username";
        var password = "1234";
        var email = "test-email";
        var nickname = "test-nickname";
        var token = "test-token";
        var signupCommand = new MemberSignupCommand(username, password, email, nickname, token);
        var credentialCreateCommand = new MemberCredentialCreateCommand(signupCommand.username(), signupCommand.password(), signupCommand.email(), token);
        var memberProfileRequest = new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
        when(memberService.createCredential(credentialCreateCommand)).thenReturn(new CreateCredentialResult.Success(id));
        when(memberApiClient.createProfile(id, memberProfileRequest)).thenThrow(new FeignException.BadRequest("msg", createProfileRequest(), null, null));
        doThrow(new RuntimeException("database-error")).when(memberCredentialTxWriter).hardDeleteCredential(id);

        assertThatThrownBy(() -> memberSignupOrchestrator.coordinateSignup(signupCommand))
                .isExactlyInstanceOf(MemberCredentialCompensationFailedException.class);

        verify(memberApiClient).createProfile(id, memberProfileRequest);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
    }

    private Request createProfileRequest() {
        return Request.create(Request.HttpMethod.PUT, "url", Collections.emptyMap(), null, null, null);
    }
}