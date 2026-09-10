package com.parking.common.exception;

public class ParkingSpotAlreadyRegisteredException extends BusinessException {

    public ParkingSpotAlreadyRegisteredException(String message) {
        super(ErrorCode.PARKING_SPOT_ALREADY_REGISTERED, message);
    }
}