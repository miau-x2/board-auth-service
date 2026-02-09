package com.example.board.auth.credential.service;

import com.example.board.auth.commons.utils.DatabaseConstraintName;
import com.example.board.auth.credential.entity.MemberCredential;
import com.example.board.auth.credential.repository.MemberCredentialRepository;
import com.example.board.auth.credential.service.command.MemberCredentialCreateCommand;
import com.example.board.auth.credential.service.command.MemberCredentialSaveCommand;
import com.example.board.auth.credential.service.impl.MemberServiceImpl;
import com.example.board.auth.credential.service.result.ActivateCredentialResult;
import com.example.board.auth.credential.service.result.CreateCredentialResult;
import com.example.board.auth.credential.service.result.EmailAvailabilityResult;
import com.example.board.auth.credential.service.result.UsernameAvailabilityResult;
import com.example.board.auth.credential.tx.MemberCredentialTxWriter;
import com.example.board.auth.mail.repository.EmailAuthenticationRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberCredentialRepository memberCredentialRepository;
    @Mock
    private EmailAuthenticationRepository emailAuthenticationRepository;
    @Mock
    private MemberCredentialTxWriter memberCredentialTxWriter;
    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("회원 자격 증명 생성 - 성공")
    void should_ReturnSuccess_When_MemberCredentialSaveSuccessfully() {
        var id = 1L;
        var token = "test-token";
        var email = "test@gmail.com";
        var createCommand = new MemberCredentialCreateCommand("test", "1234", email, token);
        var saveCommand = new MemberCredentialSaveCommand(createCommand.username(), createCommand.password(), createCommand.email());
        when(emailAuthenticationRepository.useSignupToken(token)).thenReturn(email);
        when(memberCredentialTxWriter.save(saveCommand)).thenReturn(id);

        var actual = memberService.createCredential(createCommand);
        assertThat(actual)
                .isExactlyInstanceOf(CreateCredentialResult.Success.class)
                .satisfies(result -> {
                    var res = (CreateCredentialResult.Success) result;
                    assertThat(res.id()).isEqualTo(id);
                });

        verify(emailAuthenticationRepository).useSignupToken(token);
        verify(memberCredentialTxWriter).save(saveCommand);
    }

    @Test
    @DisplayName("회원 자격 증명 생성 - 실패(지원하지 않는 이메일 도메인)")
    void should_ReturnEmailDomainNotAllowed_When_DomainIsNotSupported() {
        var token = "test-token";
        var email = "test@example.com";
        var createCommand = new MemberCredentialCreateCommand("test", "1234", email, token);

        var actual = memberService.createCredential(createCommand);
        assertThat(actual)
                .isExactlyInstanceOf(CreateCredentialResult.EmailDomainNotAllowed.class);

        verify(emailAuthenticationRepository, never()).useSignupToken(token);
        verify(memberCredentialTxWriter, never()).save(any(MemberCredentialSaveCommand.class));
    }

    @Test
    @DisplayName("회원 자격 증명 생성 - 실패(이메일 인증 토큰 만료)")
    void should_ReturnTokenExpired_When_EmailAuthTokenIsExpired() {
        var token = "test-token";
        var email = "test@gmail.com";
        var createCommand = new MemberCredentialCreateCommand("test", "1234", email, token);
        when(emailAuthenticationRepository.useSignupToken(token)).thenReturn(null);

        var actual = memberService.createCredential(createCommand);
        assertThat(actual)
                .isExactlyInstanceOf(CreateCredentialResult.TokenExpired.class);

        verify(emailAuthenticationRepository).useSignupToken(token);
        verify(memberCredentialTxWriter, never()).save(any(MemberCredentialSaveCommand.class));
    }

    @Test
    @DisplayName("회원 자격 증명 생성 - 실패(유효하지 않은 이메일 인증 토큰)")
    void should_ReturnTokenInvalid_When_EmailAuthTokenIsInvalid() {
        var token = "test-token";
        var email = "test@gmail.com";
        var createCommand = new MemberCredentialCreateCommand("test", "1234", email, token);
        when(emailAuthenticationRepository.useSignupToken(token)).thenReturn("email");

        var actual = memberService.createCredential(createCommand);
        assertThat(actual)
                .isExactlyInstanceOf(CreateCredentialResult.TokenInvalid.class);

        verify(emailAuthenticationRepository).useSignupToken(token);
        verify(memberCredentialTxWriter, never()).save(any(MemberCredentialSaveCommand.class));
    }

    @Test
    @DisplayName("회원 자격 증명 생성 - 실패(아이디 중복)")
    void should_ReturnUsernameAlreadyExists_When_UsernameIsDuplicated() {
        var token = "test-token";
        var email = "test@gmail.com";
        var createCommand = new MemberCredentialCreateCommand("test", "1234", email, token);
        when(emailAuthenticationRepository.useSignupToken(token)).thenReturn(email);
        when(memberCredentialRepository.existsByUsername(createCommand.username())).thenReturn(true);

        var actual = memberService.createCredential(createCommand);
        assertThat(actual)
                .isExactlyInstanceOf(CreateCredentialResult.UsernameAlreadyExists.class);

        verify(emailAuthenticationRepository).useSignupToken(token);
        verify(memberCredentialRepository).existsByUsername(createCommand.username());
        verify(memberCredentialRepository, never()).existsByEmail(createCommand.email());
        verify(memberCredentialTxWriter, never()).save(any(MemberCredentialSaveCommand.class));
    }

    @Test
    @DisplayName("회원 자격 증명 생성 - 실패(아이디 중복 - 아이디 유니크 제약 조건 예외)")
    void should_ReturnUsernameAlreadyExists_When_UniqueConstraintViolationException() {
        var token = "test-token";
        var email = "test@gmail.com";
        var createCommand = new MemberCredentialCreateCommand("test", "1234", email, token);
        var saveCommand = new MemberCredentialSaveCommand(createCommand.username(), createCommand.password(), createCommand.email());
        when(emailAuthenticationRepository.useSignupToken(token)).thenReturn(email);
        when(memberCredentialRepository.existsByUsername(createCommand.username())).thenReturn(false);
        when(memberCredentialRepository.existsByEmail(createCommand.email())).thenReturn(false);
        var uniqueConstraintViolationException = createDataIntegrityViolationException(DatabaseConstraintName.MemberCredential.USERNAME);
        when(memberCredentialTxWriter.save(saveCommand)).thenThrow(uniqueConstraintViolationException);

        var actual = memberService.createCredential(createCommand);
        assertThat(actual)
                .isExactlyInstanceOf(CreateCredentialResult.UsernameAlreadyExists.class);

        verify(emailAuthenticationRepository).useSignupToken(token);
        verify(memberCredentialRepository).existsByUsername(createCommand.username());
        verify(memberCredentialRepository).existsByEmail(createCommand.email());
        verify(memberCredentialTxWriter).save(any(MemberCredentialSaveCommand.class));
    }

    @Test
    @DisplayName("회원 자격 증명 생성 - 실패(이메일 중복)")
    void should_ReturnEmailAlreadyExists_When_EmailIsDuplicated() {
        var token = "test-token";
        var email = "test@gmail.com";
        var createCommand = new MemberCredentialCreateCommand("test", "1234", email, token);
        when(emailAuthenticationRepository.useSignupToken(token)).thenReturn(email);
        when(memberCredentialRepository.existsByUsername(createCommand.username())).thenReturn(false);
        when(memberCredentialRepository.existsByEmail(createCommand.email())).thenReturn(true);

        var actual = memberService.createCredential(createCommand);
        assertThat(actual)
                .isExactlyInstanceOf(CreateCredentialResult.EmailAlreadyExists.class);

        verify(emailAuthenticationRepository).useSignupToken(token);
        verify(memberCredentialRepository).existsByUsername(createCommand.username());
        verify(memberCredentialRepository).existsByEmail(createCommand.email());
        verify(memberCredentialTxWriter, never()).save(any(MemberCredentialSaveCommand.class));
    }

    @Test
    @DisplayName("회원 자격 증명 생성 - 실패(아이디 중복 - 이메일 유니크 제약 조건 예외)")
    void should_ReturnEmailAlreadyExists_When_UniqueConstraintViolationException() {
        var token = "test-token";
        var email = "test@gmail.com";
        var createCommand = new MemberCredentialCreateCommand("test", "1234", email, token);
        var saveCommand = new MemberCredentialSaveCommand(createCommand.username(), createCommand.password(), createCommand.email());
        when(emailAuthenticationRepository.useSignupToken(token)).thenReturn(email);
        when(memberCredentialRepository.existsByUsername(createCommand.username())).thenReturn(false);
        when(memberCredentialRepository.existsByEmail(createCommand.email())).thenReturn(false);
        var uniqueConstraintViolationException = createDataIntegrityViolationException(DatabaseConstraintName.MemberCredential.EMAIL);
        when(memberCredentialTxWriter.save(saveCommand)).thenThrow(uniqueConstraintViolationException);

        var actual = memberService.createCredential(createCommand);
        assertThat(actual)
                .isExactlyInstanceOf(CreateCredentialResult.EmailAlreadyExists.class);

        verify(emailAuthenticationRepository).useSignupToken(token);
        verify(memberCredentialRepository).existsByUsername(createCommand.username());
        verify(memberCredentialRepository).existsByEmail(createCommand.email());
        verify(memberCredentialTxWriter).save(any(MemberCredentialSaveCommand.class));
    }

    @Test
    @DisplayName("회원 자격 증명 생성 - 실패(처리하지 않는 무결성 예외)")
    void should_ThrowUnhandledDataIntegrityViolationException_When_DataIntegrityViolationException() {
        var token = "test-token";
        var email = "test@gmail.com";
        var createCommand = new MemberCredentialCreateCommand("test", "1234", email, token);
        var saveCommand = new MemberCredentialSaveCommand(createCommand.username(), createCommand.password(), createCommand.email());
        when(emailAuthenticationRepository.useSignupToken(token)).thenReturn(email);
        when(memberCredentialRepository.existsByUsername(createCommand.username())).thenReturn(false);
        when(memberCredentialRepository.existsByEmail(createCommand.email())).thenReturn(false);
        var exception = createDataIntegrityViolationException("test");

        when(memberCredentialTxWriter.save(saveCommand)).thenThrow(exception);

        assertThatThrownBy(() -> memberService.createCredential(createCommand))
                .hasCauseInstanceOf(DataIntegrityViolationException.class);

        verify(emailAuthenticationRepository).useSignupToken(token);
        verify(memberCredentialRepository).existsByUsername(createCommand.username());
        verify(memberCredentialRepository).existsByEmail(createCommand.email());
        verify(memberCredentialTxWriter).save(any(MemberCredentialSaveCommand.class));
    }

    @Test
    @DisplayName("회원 자격 증명 활성화 - 성공")
    void should_ReturnSuccess_When_MemberCredentialStatusIsPending() {
        var id = 1L;
        var credential = MemberCredential.createMember("test", "1234", "test@example.com");
        when(memberCredentialRepository.findById(id))
                .thenReturn(Optional.of(credential));

        var actual = memberService.activateCredential(id);
        assertThat(actual)
                .isExactlyInstanceOf(ActivateCredentialResult.Success.class);

        verify(memberCredentialRepository).findById(id);
    }

    @Test
    @DisplayName("회원 자격 증명 활성화 - 실패(ACTIVE 상태)")
    void should_ReturnFailure_When_MemberCredentialStatusIsNotPending() {
        var id = 1L;
        var credential = MemberCredential.createMember("test", "1234", "test@example.com");
        credential.activate();
        when(memberCredentialRepository.findById(id))
                .thenReturn(Optional.of(credential));

        var actual = memberService.activateCredential(id);
        assertThat(actual)
                .isExactlyInstanceOf(ActivateCredentialResult.Failure.class);

        verify(memberCredentialRepository).findById(id);
    }

    @Test
    @DisplayName("회원 자격 증명 활성화 - 실패(존재하지 않는 회원)")
    void should_ReturnNotFound_When_MemberCredentialIsNotExists() {
        var id = 1L;
        when(memberCredentialRepository.findById(id))
                .thenReturn(Optional.empty());

        var actual = memberService.activateCredential(id);
        assertThat(actual)
                .isExactlyInstanceOf(ActivateCredentialResult.NotFound.class);

        verify(memberCredentialRepository).findById(id);
    }

    @Test
    @DisplayName("회원 자격 증명 삭제 - 성공")
    void should_DeleteSuccessfully_When_MemberCredentialExists() {
        var id = 1L;
        memberService.deleteCredential(id);
        verify(memberCredentialRepository).deleteById(id);
    }

    @Test
    @DisplayName("사용 가능한 아이디 검증 - 사용 가능")
    void should_ReturnAvailable_When_UsernameIsNotExists() {
        var username = "test-username";
        when(memberCredentialRepository.existsByUsername(username)).thenReturn(false);

        var actual = memberService.checkUsernameAvailability(username);
        assertThat(actual)
                .isExactlyInstanceOf(UsernameAvailabilityResult.Available.class)
                .satisfies(result -> {
                    var res = (UsernameAvailabilityResult.Available) result;
                    assertThat(res.message()).isEqualTo("사용 가능한 아이디입니다.");
                });

        verify(memberCredentialRepository).existsByUsername(username);
    }

    @Test
    @DisplayName("사용 가능한 아이디 검증 - 아이디 중복")
    void should_ReturnUnavailable_When_UsernameIsDuplicated() {
        var username = "test-username";
        when(memberCredentialRepository.existsByUsername(username)).thenReturn(true);

        var actual = memberService.checkUsernameAvailability(username);
        assertThat(actual)
                .isExactlyInstanceOf(UsernameAvailabilityResult.Unavailable.class)
                .satisfies(result -> {
                    var res = (UsernameAvailabilityResult.Unavailable) result;
                    assertThat(res.message()).isEqualTo("이미 사용 중인 아이디입니다.");
                });

        verify(memberCredentialRepository).existsByUsername(username);
    }

    @Test
    @DisplayName("사용 가능한 이메일 검증 - 사용 가능")
    void should_ReturnAvailable_When_EmailIsNotExists() {
        var email = "test@gmail.com";
        when(memberCredentialRepository.existsByEmail(email)).thenReturn(false);

        var actual = memberService.checkEmailAvailability(email);
        assertThat(actual)
                .isExactlyInstanceOf(EmailAvailabilityResult.Available.class)
                .satisfies(result -> {
                    var res = (EmailAvailabilityResult.Available) result;
                    assertThat(res.message()).isEqualTo("사용 가능한 이메일입니다.");
                });

        verify(memberCredentialRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("사용 가능한 이메일 검증 - 지원하지 않는 이메일 도메인")
    void should_ReturnUnavailable_When_DomainIsNotSupported() {
        var email = "test-email";

        var actual = memberService.checkEmailAvailability(email);
        assertThat(actual)
                .isExactlyInstanceOf(EmailAvailabilityResult.Unavailable.class)
                .satisfies(result -> {
                    var res = (EmailAvailabilityResult.Unavailable) result;
                    assertThat(res.message()).isEqualTo("지메일과 네이버메일만 사용할 수 있습니다.");
                });

        verify(memberCredentialRepository, never()).existsByEmail(email);
    }

    @Test
    @DisplayName("사용 가능한 이메일 검증 - 이메일 중복")
    void should_ReturnUnavailable_When_EmailIsDuplicated() {
        var email = "test@gmail.com";
        when(memberCredentialRepository.existsByEmail(email)).thenReturn(true);

        var actual = memberService.checkEmailAvailability(email);
        assertThat(actual)
                .isExactlyInstanceOf(EmailAvailabilityResult.Unavailable.class)
                .satisfies(result -> {
                    var res = (EmailAvailabilityResult.Unavailable) result;
                    assertThat(res.message()).isEqualTo("이미 사용 중인 이메일입니다.");
                });

        verify(memberCredentialRepository).existsByEmail(email);
    }

    private DataIntegrityViolationException createDataIntegrityViolationException(String constraintName) {
        var cve = new ConstraintViolationException("msg", null, constraintName);
        return new DataIntegrityViolationException("msg", cve);
    }
}