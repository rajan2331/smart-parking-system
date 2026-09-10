package com.parking.common.exception;

public class VehicleNotFoundException extends BusinessException {

    public VehicleNotFoundException(String message) {
        super(ErrorCode.VEHICLE_NOT_FOUND, message);
    }
}