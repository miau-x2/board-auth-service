package com.example.board.auth.commons.utils;

import com.example.board.auth.commons.response.ApiCode;
import com.example.board.auth.commons.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public final class ResponseUtils {
    private ResponseUtils() {}

    public static <T> ResponseEntity<ApiResponse<T>> successResponse(ApiCode code) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.success(code));
    }

    public static <T> ResponseEntity<ApiResponse<T>> successResponse(ApiCode code, T data) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.success(code, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> errorResponse(ApiCode code) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(code));
    }
}
