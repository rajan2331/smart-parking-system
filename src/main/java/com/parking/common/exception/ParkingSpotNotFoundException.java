package com.parking.common.exception;

public class ParkingSpotNotFoundException extends BusinessException {

    public ParkingSpotNotFoundException(String message) {
        super(ErrorCode.PARKING_SPOT_NOT_FOUND, message);
    }
}
