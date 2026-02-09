package com.example.board.auth.commons.response;

public record ApiResponse<T>(boolean success, String code, String message, T data) {
    public static <T> ApiResponse<T> success(ApiCode code) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), null);
    }
    public static <T> ApiResponse<T> success(ApiCode code,  T data) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), data);
    }
    public static <T> ApiResponse<T> error(ApiCode code) {
        return new ApiResponse<>(false, code.getCode(), code.getMessage(), null);
    }
}
