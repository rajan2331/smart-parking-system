package com.parking.common.exception;

public enum ErrorCode {

    EMAIL_ALREADY_REGISTERED("EMAIL_ALREADY_REGISTERED"),
    MOBILE_ALREADY_REGISTERED("MOBILE_ALREADY_REGISTERED"),
    USER_REGISTERED("USER_REGISTERED"),
    VALIDATION_FAILED("VALIDATION_FAILED"),
	VEHICLE_REGISTERED("VEHICLE REGISTERED"),
	VEHICLE_ALREADY_REGISTERED("VEHICLE ALREADY REGISTERED"), USER_NOT_FOUND("USER NOT FOUND");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
