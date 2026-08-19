package com.parking.common.exception;

public class MobileAlreadyRegisteredException extends BusinessException {

    public MobileAlreadyRegisteredException(String message) {
        super(ErrorCode.MOBILE_ALREADY_REGISTERED,message);
    }

}
