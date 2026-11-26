package com.parking.common.exception;

public class ReservationNotFoundException extends BusinessException {

    public ReservationNotFoundException(String message) {
        super(ErrorCode.RESERVATION_NOT_FOUND, message);
    }
}
