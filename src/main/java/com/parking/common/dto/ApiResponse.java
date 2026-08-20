package com.parking.common.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(
            String code,
            String message,
            T data) {

        return new ApiResponse<>(
                true,
                code,
                message,
                data
        );
    }

    public static <T> ApiResponse<T> failure(
            String code,
            String message,
            T data) {

        return new ApiResponse<>(
                false,
                code,
                message,
                data
        );
    }
}