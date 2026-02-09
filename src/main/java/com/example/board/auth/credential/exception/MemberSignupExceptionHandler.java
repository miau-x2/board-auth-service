package com.example.board.auth.credential.exception;

import com.example.board.auth.credential.controller.MemberSignupController;
import com.example.board.auth.credential.controller.dto.request.MemberSignupRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice(assignableTypes = MemberSignupController.class)
public class MemberSignupExceptionHandler {
    @ExceptionHandler(MemberProfileCompensationFailedException.class)
    public String handleMemberProfileCompensationFailedException(MemberProfileCompensationFailedException e, Model model) {
        log.error("회원 프로필 보상 트랜잭션 실패: {}, 회원 프로필과 자격 증명 삭제 필요.", e.getId(), e);
        return signupGlobalError(model);
    }

    @ExceptionHandler(MemberCredentialCompensationFailedException.class)
    public String handleMemberCredentialCompensationFailedException(MemberCredentialCompensationFailedException e, Model model) {
        log.error("회원 자격 증명 보상 트랜잭션 실패: {} 회원 자격 증명 삭제 필요.", e.getId(), e);
        return signupGlobalError(model);
    }

    @ExceptionHandler(DataAccessException.class)
    public String handleDataAccessException(DataAccessException e, Model model) {
        log.error("처리 되지 않은 데이터 접근 계층 예외 발생: {}", e.getMessage(), e);
        return signupGlobalError(model);
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("처리 되지 않은 예외 발생: {}", e.getMessage(), e);
        return signupGlobalError(model);
    }

    private void addGlobalError(Model model) {
        model.addAttribute("globalError", "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");

        if(!model.containsAttribute("signupFormData")) {
            model.addAttribute("signupFormData", MemberSignupRequest.empty());
        }
    }

    private String signupGlobalError(Model model) {
        addGlobalError(model);
        return "member/signup";
    }
}
