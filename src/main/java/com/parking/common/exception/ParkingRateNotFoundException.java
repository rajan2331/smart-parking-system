package com.parking.common.exception;


public class ParkingRateNotFoundException extends BusinessException {

    public ParkingRateNotFoundException(String message) {
        super(ErrorCode.PARKING_RATE_NOT_FOUND, message);
    }
}
