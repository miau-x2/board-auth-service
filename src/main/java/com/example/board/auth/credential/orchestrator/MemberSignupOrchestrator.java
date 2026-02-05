package com.example.board.auth.credential.orchestrator;

import com.example.board.auth.client.exception.FeignExceptions;
import com.example.board.auth.client.member.MemberApiClient;
import com.example.board.auth.client.member.MemberProfileCreateRequest;
import com.example.board.auth.commons.exception.RetryableRemoteException;
import com.example.board.auth.commons.response.ApiResponse;
import com.example.board.auth.commons.utils.MemberServiceErrorCode;
import com.example.board.auth.credential.exception.MemberCredentialCompensationFailedException;
import com.example.board.auth.credential.exception.MemberProfileCompensationFailedException;
import com.example.board.auth.credential.service.MemberService;
import com.example.board.auth.credential.service.command.MemberCredentialCreateCommand;
import com.example.board.auth.credential.service.command.MemberSignupCommand;
import com.example.board.auth.credential.service.result.*;
import com.example.board.auth.credential.tx.MemberCredentialTxWriter;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberSignupOrchestrator {
    private final MemberApiClient memberApiClient;
    private final MemberService memberService;
    private final RetryTemplate retryTemplate;
    private final FeignExceptions feignExceptions;
    private final MemberCredentialTxWriter memberCredentialTxWriter;

    public MemberSignupOrchestrator(MemberApiClient memberApiClient,
                                    MemberService memberService,
                                    @Qualifier("memberApiRetryTemplate") RetryTemplate retryTemplate,
                                    FeignExceptions feignExceptions, MemberCredentialTxWriter memberCredentialTxWriter) {
        this.memberApiClient = memberApiClient;
        this.memberService = memberService;
        this.retryTemplate = retryTemplate;
        this.feignExceptions = feignExceptions;
        this.memberCredentialTxWriter = memberCredentialTxWriter;
    }

    public SignupResult coordinateSignup(MemberSignupCommand command) {
        // 회원 자격 증명 생성
        var signupResult = memberService.createCredential(new MemberCredentialCreateCommand(command.username(), command.password(), command.email(), command.token()));

        return switch (signupResult) {
            case CreateCredentialResult.Success(var id) -> processProfileCreation(id, command.username(), command.nickname());
            case CreateCredentialResult.EmailDomainNotAllowed _ -> new SignupResult.EmailDomainNotAllowed();
            case CreateCredentialResult.TokenExpired _ -> new SignupResult.TokenExpired();
            case CreateCredentialResult.TokenInvalid _ -> new SignupResult.TokenInvalid();
            case CreateCredentialResult.EmailAlreadyExists _ -> new SignupResult.EmailAlreadyExists();
            case CreateCredentialResult.UsernameAlreadyExists _ -> new SignupResult.UsernameAlreadyExists();
        };
    }

    private SignupResult processProfileCreation(Long id, String username, String nickname) {
        var profileResult = callCreateProfileApi(id, username, nickname);

        return switch (profileResult) {
            case CreateProfileResult.Success _ ->
                    processCredentialActivation(id);
            case CreateProfileResult.HandleDuplicate _ -> {
                compensateCredentialCreation(id);
                yield new SignupResult.UsernameAlreadyExists();
            }
            case CreateProfileResult.NicknameDuplicate _ -> {
                compensateCredentialCreation(id);
                yield new SignupResult.NicknameAlreadyExists();
            }
            case CreateProfileResult.UnexpectedValidationError _ -> {
                compensateCredentialCreation(id);
                yield new SignupResult.UnexpectedValidationError();
            }
            case CreateProfileResult.UnexpectedConflictError _ -> {
                compensateCredentialCreation(id);
                yield new SignupResult.UnexpectedConflictError();
            }
            // 회원 프로필이 생성 되었지만 네트 워크 오류로 실패 가능성 -> 보상 트랜잭션으로 회원 프로필 삭제
            case CreateProfileResult.DownstreamServiceError _ -> {
                compensateSignup(id);
                yield new SignupResult.DownstreamServiceError();
            }
        };
    }

    private CreateProfileResult callCreateProfileApi(Long id, String username, String nickname) {
        try {
            return retryTemplate.execute(() -> {
                try {
                    memberApiClient.createProfile(id, new MemberProfileCreateRequest(username, nickname));
                    return new CreateProfileResult.Success();
                } catch (FeignException.BadRequest _) {
                    return new CreateProfileResult.UnexpectedValidationError();
                } catch (FeignException.Conflict e) {
                    return feignExceptions.extractErrorResponse(e)
                            .map(ApiResponse::code)
                            .map(code -> {
                                if(MemberServiceErrorCode.HANDLE_DUPLICATE.equals(code)) {
                                    return new CreateProfileResult.HandleDuplicate();
                                }
                                if(MemberServiceErrorCode.NICKNAME_DUPLICATE.equals(code)) {
                                    return new CreateProfileResult.NicknameDuplicate();
                                }
                                return new CreateProfileResult.UnexpectedConflictError();
                            })
                            .orElseGet(CreateProfileResult.UnexpectedConflictError::new);
                }
                catch (FeignException e) {
                    if(feignExceptions.isRetryableStatus(e.status())) {
                        throw new RetryableRemoteException(e);
                    }
                    return new CreateProfileResult.DownstreamServiceError();
                }
            });

        } catch (RetryException e) {
            log.error("회원 프로필 생성 재시도 모두 실패: {}", id, e);
            return new CreateProfileResult.DownstreamServiceError();
        }
    }

    private SignupResult processCredentialActivation(Long id) {
        var result = memberService.activateCredential(id);

        return switch (result) {
            case ActivateCredentialResult.Success _ -> new SignupResult.Success();
            // 회원 프로필 활성화 실패 -> 보상 트랜잭션으로 회원 프로필 삭제 후 회원 자격 증명 삭제
            case ActivateCredentialResult.NotFound _, ActivateCredentialResult.Failure _ -> {
                compensateSignup(id);
                yield new SignupResult.SystemError();
            }
        };
    }

    private DeleteProfileResult callDeleteProfileApi(Long id) {
        try {
            return retryTemplate.execute(() -> {
                try {
                    memberApiClient.hardDeleteProfile(id);
                    return new DeleteProfileResult.Success();
                } catch (FeignException e) {
                    if(feignExceptions.isRetryableStatus(e.status())) {
                        throw new RetryableRemoteException(e);
                    }
                    return new DeleteProfileResult.DownstreamServiceError();
                }
            });
        } catch (RetryException e) {
            return new DeleteProfileResult.DownstreamServiceError();
        }
    }

    // 회원 프로필 삭제 후 회원 자격 증명 삭제하는 메서드로 회원 생성 과정 롤백
    private void compensateSignup(Long id) {
        var result = callDeleteProfileApi(id);

        switch (result) {
          case DeleteProfileResult.Success _ -> compensateCredentialCreation(id);

          case DeleteProfileResult.DownstreamServiceError _ -> {
              // 보상 트랜잭션 회원 프로필 삭제 실패
              log.error("보상 트랜잭션 회원 프로필 삭제 실패: {}", id);
              // 보상 트랜잭션이 실패 했으므로 회원 프로필 삭제 후 자격 증명 삭제 처리 할 별도의 기능 추가 필요
              throw new MemberProfileCompensationFailedException(id);
          }
        };
    }

    private void compensateCredentialCreation(Long id) {
        // 회원 프로필 생성 실패 또는 삭제 성공 -> 보상 트랜잭션으로 회원 자격 증명 삭제
        try {
            memberCredentialTxWriter.hardDeleteCredential(id);
        } catch (Exception e) {
            log.error("보상 트랜잭션: 회원 자격 증명 삭제 실패: {}", id, e);
            throw new MemberCredentialCompensationFailedException(id, e);
        }
    }
}
