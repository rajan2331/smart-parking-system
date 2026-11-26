package com.parking.common.exception;

/** A walk-in tried to take a spot that is reserved now or soon -> 409. */
public class ParkingSpotReservedException extends BusinessException {

    public ParkingSpotReservedException(String message) {
        super(ErrorCode.PARKING_SPOT_RESERVED, message);
    }
}
