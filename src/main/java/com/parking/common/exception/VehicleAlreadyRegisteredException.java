package com.parking.common.exception;

public class VehicleAlreadyRegisteredException  extends BusinessException {

    public VehicleAlreadyRegisteredException(String message) {
        super(ErrorCode.VEHICLE_ALREADY_REGISTERED,message);
    }

}

