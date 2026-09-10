package com.parking.common.exception;

public class ParkingSpotNotAvailableException extends BusinessException {

    public ParkingSpotNotAvailableException(String message) {
        super(ErrorCode.PARKING_SPOT_NOT_AVAILABLE, message);
    }
}
