package com.example.board.auth.commons.exception;

import com.example.board.auth.authentication.token.exception.RefreshTokenRemovalFailedException;
import com.example.board.auth.authentication.token.exception.RefreshTokenRotateFailedException;
import com.example.board.auth.commons.response.ApiCode;
import com.example.board.auth.commons.response.ApiResponse;
import com.example.board.auth.commons.response.AuthenticationErrorCode;
import com.example.board.auth.commons.response.CommonErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var errors = new ArrayList<FieldValidationError>();

        for (var fieldError : e.getBindingResult().getFieldErrors()) {
            var field = fieldError.getField();
            var message = fieldError.getDefaultMessage();
            if (message != null) {
                errors.add(new FieldValidationError(field, message));
            }
        }

        var message = errors.stream()
                .map(FieldValidationError::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(CommonErrorCode.INPUT_INVALID.getMessage());
        return handleValidationError(CommonErrorCode.INPUT_INVALID, message, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException e) {
        var errors = e.getConstraintViolations().stream()
                .map(violation -> new FieldValidationError(extractField(violation.getPropertyPath().toString()), violation.getMessage()))
                .toList();

        var message = errors.stream()
                .map(FieldValidationError::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(CommonErrorCode.INPUT_INVALID.getMessage());
        return handleValidationError(CommonErrorCode.INPUT_INVALID, message, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        var errors = new ArrayList<FieldValidationError>();

        for (var result : e.getParameterValidationResults()) {
            var field = result.getMethodParameter().getParameterName();
            for (var error : result.getResolvableErrors()) {
                var message = error.getDefaultMessage();
                if (message != null) {
                    errors.add(new FieldValidationError(field, message));
                }
            }
        }

        var message = errors.stream()
                .map(FieldValidationError::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(CommonErrorCode.INPUT_INVALID.getMessage());
        return handleValidationError(CommonErrorCode.INPUT_INVALID, message, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformed() {
        return handleError(CommonErrorCode.REQUEST_MALFORMED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch() {
        return handleError(CommonErrorCode.REQUEST_MALFORMED);
    }

    @ExceptionHandler(RefreshTokenRemovalFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenRemovalFailedException(RefreshTokenRemovalFailedException e) {
        log.error("리프레시 토큰 삭제 실패: {}", e.getMessage(), e);
        return handleError(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RefreshTokenRotateFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenRotateFailedException(RefreshTokenRotateFailedException e) {
        log.error("리프레시 토큰 회전 실패: {}", e.getMessage(), e);
        return handleError(AuthenticationErrorCode.TOKEN_REISSUE_TEMPORARILY_UNAVAILABLE);
    }

    @ExceptionHandler(UnhandledDataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledDataIntegrityViolationException(UnhandledDataIntegrityViolationException e) {
        log.error("처리 되지 않은 무결성 예외 발생: {}", e.getMessage(), e);
        return handleError(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException e) {
        log.error("처리 되지 않은 데이터 접근 계층 예외 발생: {}", e.getMessage(), e);
        return handleError(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("처리 되지 않은 예외 발생: {}", e.getMessage(), e);
        return handleError(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> handleError(ApiCode code) {
        ApiResponse<Void> body = ApiResponse.error(code);
        return ResponseEntity.status(code.getHttpStatus()).body(body);
    }

    private ResponseEntity<ApiResponse<Object>> handleValidationError(ApiCode code, String message, List<FieldValidationError> errors) {
        var payload = new ValidationErrorPayload(errors);
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(new ApiResponse<>(false, code.getCode(), message, payload));
    }

    private String extractField(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return null;
        }
        var path = propertyPath.trim();
        var idx = path.lastIndexOf('.');
        if (idx < 0 || idx == path.length() - 1) {
            return path;
        }
        return path.substring(idx + 1);
    }

    private record ValidationErrorPayload(List<FieldValidationError> errors) {}
    private record FieldValidationError(String field, String message) {}
}
