package com.parking.common.exception;

public class ParkingSessionNotFoundException extends BusinessException {

    public ParkingSessionNotFoundException(String message) {
        super(ErrorCode.PARKING_SESSION_NOT_FOUND, message);
    }
}