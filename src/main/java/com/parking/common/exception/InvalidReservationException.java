package com.parking.common.exception;

/** The request itself breaks a booking rule (times, duration, how far ahead) -> 400. */
public class InvalidReservationException extends BusinessException {

    public InvalidReservationException(String message) {
        super(ErrorCode.INVALID_RESERVATION, message);
    }
}
