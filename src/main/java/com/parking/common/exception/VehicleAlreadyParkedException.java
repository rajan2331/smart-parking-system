package com.parking.common.exception;

public class VehicleAlreadyParkedException extends BusinessException {

    public VehicleAlreadyParkedException(String message) {
        super(ErrorCode.VEHICLE_ALREADY_PARKED, message);
    }
}