package com.example.board.auth.client.member;

import com.example.board.auth.client.exception.FeignExceptions;
import com.example.board.auth.commons.exception.RetryableCreateProfileException;
import com.example.board.auth.commons.exception.RetryableDeleteProfileException;
import com.example.board.auth.commons.response.ApiResponse;
import com.example.board.auth.commons.utils.MemberServiceErrorCode;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberProfileFeignAdapter {
    private final MemberApiClient memberApiClient;
    private final RetryTemplate retryTemplate;
    private final FeignExceptions feignExceptions;

    public MemberProfileFeignAdapter(
            MemberApiClient memberApiClient,
            @Qualifier("memberApiRetryTemplate")
            RetryTemplate retryTemplate,
            FeignExceptions feignExceptions) {
        this.memberApiClient = memberApiClient;
        this.retryTemplate = retryTemplate;
        this.feignExceptions = feignExceptions;
    }

    public CreateProfileClientResult createProfile(Long id, MemberProfileCreateRequest request) {
        try {
            return retryTemplate.execute(() -> {
                try {
                    memberApiClient.createProfile(id, request);
                    return new CreateProfileClientResult.Success();
                } catch (FeignException.BadRequest e) {
                    log.error("인증 서버와 회원 서버의 입력값 검증 정책 상이.", e);
                    return new CreateProfileClientResult.UnexpectedValidationError();
                } catch (FeignException.Conflict e) {
                    return feignExceptions.extractErrorResponse(e)
                            .map(ApiResponse::code)
                            .map(code -> {
                                if(MemberServiceErrorCode.HANDLE_DUPLICATE.equals(code)) {
                                    return new CreateProfileClientResult.HandleDuplicate();
                                }
                                if(MemberServiceErrorCode.NICKNAME_DUPLICATE.equals(code)) {
                                    return new CreateProfileClientResult.NicknameDuplicate();
                                }
                                log.error("회원 서버에서 정의되지 않은 409 응답.", e);
                                return new CreateProfileClientResult.UnexpectedConflictError();
                            })
                            .orElseGet(CreateProfileClientResult.UnexpectedConflictError::new);
                } catch (FeignException e) {
                    if(feignExceptions.isRetryableStatus(e.status())) {
                        throw new RetryableCreateProfileException(e);
                    }
                    return new CreateProfileClientResult.DownstreamServiceError();
                }
            });
        } catch (RetryException e) {
            log.error("회원 프로필 생성 재시도 모두 실패: {}", id, e);
            return new CreateProfileClientResult.DownstreamServiceError();
        }
    }

    public DeleteProfileClientResult deleteProfile(Long id) {
        try {
            return retryTemplate.execute(() -> {
                try {
                    memberApiClient.hardDeleteProfile(id);
                    return new DeleteProfileClientResult.Success();
                } catch (FeignException e) {
                    if(feignExceptions.isRetryableStatus(e.status())) {
                        throw new RetryableDeleteProfileException(e);
                    }
                    return new DeleteProfileClientResult.DownstreamServiceError();
                }
            });
        } catch (RetryException e) {
            log.error("회원 프로필 삭제 재시도 모두 실패: {}", id, e);
            return new DeleteProfileClientResult.DownstreamServiceError();
        }
    }
}
