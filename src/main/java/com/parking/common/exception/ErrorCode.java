package com.parking.common.exception;

public enum ErrorCode {

    EMAIL_ALREADY_REGISTERED("EMAIL_ALREADY_REGISTERED"),
    MOBILE_ALREADY_REGISTERED("MOBILE_ALREADY_REGISTERED"),
    USER_REGISTERED("USER_REGISTERED"),
    VALIDATION_FAILED("VALIDATION_FAILED");
	

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
