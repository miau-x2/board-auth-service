package com.example.board.auth.commons.exception;

import com.example.board.auth.commons.response.ApiCode;
import com.example.board.auth.commons.response.ApiResponse;
import com.example.board.auth.commons.response.CommonErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException() {
        return handleError(CommonErrorCode.INPUT_INVALID);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException() {
        return handleError(CommonErrorCode.INPUT_INVALID);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformed() {
        return handleError(CommonErrorCode.REQUEST_MALFORMED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch() {
        return handleError(CommonErrorCode.REQUEST_MALFORMED);
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
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(code));
    }
}
