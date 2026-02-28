package com.example.board.auth.credential.exception;

import com.example.board.auth.commons.response.ApiResponse;
import com.example.board.auth.commons.response.CommonErrorCode;
import com.example.board.auth.credential.controller.MemberSignupController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = MemberSignupController.class)
public class MemberSignupExceptionHandler {
    @ExceptionHandler(MemberProfileCompensationFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberProfileCompensationFailedException(MemberProfileCompensationFailedException e) {
        log.error("회원 프로필 보상 트랜잭션 실패: {}, 회원 프로필과 자격 증명 삭제 필요.", e.getId(), e);
        return internalServerErrorResponse();
    }

    @ExceptionHandler(MemberCredentialCompensationFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberCredentialCompensationFailedException(MemberCredentialCompensationFailedException e) {
        log.error("회원 자격 증명 보상 트랜잭션 실패: {} 회원 자격 증명 삭제 필요.", e.getId(), e);
        return internalServerErrorResponse();
    }

    private ResponseEntity<ApiResponse<Void>> internalServerErrorResponse() {
        var code = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(code));
    }
}
