package com.example.board.auth.credential.orchestrator;

import com.example.board.auth.client.member.MemberProfileCreateRequest;
import com.example.board.auth.credential.exception.MemberCredentialCompensationFailedException;
import com.example.board.auth.credential.exception.MemberProfileCompensationFailedException;
import com.example.board.auth.credential.service.MemberCredentialService;
import com.example.board.auth.credential.service.MemberProfileService;
import com.example.board.auth.credential.service.command.MemberCredentialCreateCommand;
import com.example.board.auth.credential.service.command.MemberSignupCommand;
import com.example.board.auth.credential.service.result.ActivateCredentialResult;
import com.example.board.auth.credential.service.result.CreateCredentialResult;
import com.example.board.auth.credential.service.result.CreateProfileResult;
import com.example.board.auth.credential.service.result.DeleteProfileResult;
import com.example.board.auth.credential.service.result.SignupResult;
import com.example.board.auth.credential.tx.MemberCredentialTxWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberSignupOrchestratorTest {
    @Mock
    private MemberCredentialService memberCredentialService;
    @Mock
    private MemberProfileService memberProfileService;
    @Mock
    private MemberCredentialTxWriter memberCredentialTxWriter;
    private MemberSignupOrchestrator memberSignupOrchestrator;

    @BeforeEach
    void setUp() {
        memberSignupOrchestrator = new MemberSignupOrchestrator(memberCredentialService, memberCredentialTxWriter, memberProfileService);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 성공")
    void should_ReturnSuccess_When_SignupProcessSucceeds() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.Success());
        when(memberCredentialService.activateCredential(id))
                .thenReturn(new ActivateCredentialResult.Success());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.Success.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberCredentialService).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(지원하지 않는 이메일 도메인)")
    void should_ReturnEmailDomainNotAllowed_When_DomainIsNotSupported() {
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.EmailDomainNotAllowed());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.EmailDomainNotAllowed.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialService, never()).activateCredential(anyLong());
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(이메일 인증 토큰 만료)")
    void should_ReturnTokenExpired_When_EmailAuthTokenIsExpired() {
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.TokenExpired());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.TokenExpired.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialService, never()).activateCredential(anyLong());
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(유효하지 않은 이메일 인증 토큰)")
    void should_ReturnTokenInvalid_When_EmailAuthTokenIsInvalid() {
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.TokenInvalid());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.TokenInvalid.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialService, never()).activateCredential(anyLong());
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(이메일 중복)")
    void should_ReturnEmailAlreadyExists_When_EmailIsDuplicated() {
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.EmailAlreadyExists());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.EmailAlreadyExists.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialService, never()).activateCredential(anyLong());
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(아이디 중복)")
    void should_ReturnUsernameAlreadyExists_When_UsernameIsDuplicated() {
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.UsernameAlreadyExists());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.UsernameAlreadyExists.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService, never()).createProfile(anyLong(), any(MemberProfileCreateRequest.class));
        verify(memberCredentialService, never()).activateCredential(anyLong());
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(프로필 생성 검증 오류)")
    void should_ReturnUnexpectedValidationError_When_CreateProfileReturnsUnexpectedValidationError() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.UnexpectedValidationError());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.UnexpectedValidationError.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberCredentialService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(핸들 중복)")
    void should_ReturnUsernameAlreadyExists_When_HandleIsDuplicated() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.HandleDuplicate());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.UsernameAlreadyExists.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberCredentialService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(닉네임 중복)")
    void should_ReturnNicknameAlreadyExists_When_NicknameIsDuplicated() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.NicknameDuplicate());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.NicknameAlreadyExists.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberCredentialService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(정의되지 않은 충돌)")
    void should_ReturnUnexpectedConflictError_When_CreateProfileReturnsUnexpectedConflictError() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.UnexpectedConflictError());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.UnexpectedConflictError.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberCredentialService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(다운스트림 오류)")
    void should_ReturnDownstreamServiceError_When_CreateProfileReturnsDownstreamServiceError() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.DownstreamServiceError());
        when(memberProfileService.deleteProfile(id))
                .thenReturn(new DeleteProfileResult.Success());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.DownstreamServiceError.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberProfileService).deleteProfile(id);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberCredentialService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(자격 증명 활성화 NotFound)")
    void should_ReturnSystemError_When_ActivateCredentialReturnNotFound() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.Success());
        when(memberCredentialService.activateCredential(id))
                .thenReturn(new ActivateCredentialResult.NotFound());
        when(memberProfileService.deleteProfile(id))
                .thenReturn(new DeleteProfileResult.Success());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.SystemError.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberCredentialService).activateCredential(id);
        verify(memberProfileService).deleteProfile(id);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(자격 증명 활성화 Failure)")
    void should_ReturnSystemError_When_ActivateCredentialReturnFailure() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.Success());
        when(memberCredentialService.activateCredential(id))
                .thenReturn(new ActivateCredentialResult.Failure());
        when(memberProfileService.deleteProfile(id))
                .thenReturn(new DeleteProfileResult.Success());

        var actual = memberSignupOrchestrator.coordinateSignup(signupCommand);

        assertThat(actual).isExactlyInstanceOf(SignupResult.SystemError.class);
        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberCredentialService).activateCredential(id);
        verify(memberProfileService).deleteProfile(id);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(프로필 보상 트랜잭션 실패)")
    void should_ThrowMemberProfileCompensationFailedException_When_DeleteProfileFailsOnCreateProfileDownstreamError() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.DownstreamServiceError());
        when(memberProfileService.deleteProfile(id))
                .thenReturn(new DeleteProfileResult.DownstreamServiceError());

        assertThatThrownBy(() -> memberSignupOrchestrator.coordinateSignup(signupCommand))
                .isExactlyInstanceOf(MemberProfileCompensationFailedException.class);

        verify(memberCredentialService).createCredential(credentialCreateCommand);
        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberProfileService).deleteProfile(id);
        verify(memberCredentialTxWriter, never()).hardDeleteCredential(id);
        verify(memberCredentialService, never()).activateCredential(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(보상 자격 증명 삭제 실패: 프로필 생성 실패 경로)")
    void should_ThrowMemberCredentialCompensationFailedException_When_CredentialDeletionFailsOnCreateProfileFailure() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.UnexpectedValidationError());
        doThrow(new RuntimeException("database-error"))
                .when(memberCredentialTxWriter).hardDeleteCredential(id);

        assertThatThrownBy(() -> memberSignupOrchestrator.coordinateSignup(signupCommand))
                .isExactlyInstanceOf(MemberCredentialCompensationFailedException.class);

        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
        verify(memberCredentialService, never()).activateCredential(id);
        verify(memberProfileService, never()).deleteProfile(id);
    }

    @Test
    @DisplayName("회원 가입 오케스트레이션 - 실패(보상 자격 증명 삭제 실패: 보상 signup 경로)")
    void should_ThrowMemberCredentialCompensationFailedException_When_CredentialDeletionFailsOnCompensateSignup() {
        var id = 1L;
        var signupCommand = createSignupCommand();
        var credentialCreateCommand = toCredentialCommand(signupCommand);
        var profileCreateRequest = toProfileRequest(signupCommand);

        when(memberCredentialService.createCredential(credentialCreateCommand))
                .thenReturn(new CreateCredentialResult.Success(id));
        when(memberProfileService.createProfile(id, profileCreateRequest))
                .thenReturn(new CreateProfileResult.DownstreamServiceError());
        when(memberProfileService.deleteProfile(id))
                .thenReturn(new DeleteProfileResult.Success());
        doThrow(new RuntimeException("database-error"))
                .when(memberCredentialTxWriter).hardDeleteCredential(id);

        assertThatThrownBy(() -> memberSignupOrchestrator.coordinateSignup(signupCommand))
                .isExactlyInstanceOf(MemberCredentialCompensationFailedException.class);

        verify(memberProfileService).createProfile(id, profileCreateRequest);
        verify(memberProfileService).deleteProfile(id);
        verify(memberCredentialTxWriter).hardDeleteCredential(id);
    }

    private MemberSignupCommand createSignupCommand() {
        return new MemberSignupCommand("test-username", "1234", "test-email", "test-nickname", "test-token");
    }

    private MemberCredentialCreateCommand toCredentialCommand(MemberSignupCommand signupCommand) {
        return new MemberCredentialCreateCommand(
                signupCommand.username(),
                signupCommand.password(),
                signupCommand.email(),
                signupCommand.token()
        );
    }

    private MemberProfileCreateRequest toProfileRequest(MemberSignupCommand signupCommand) {
        return new MemberProfileCreateRequest(signupCommand.username(), signupCommand.nickname());
    }
}
