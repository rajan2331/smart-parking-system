package com.parking.common.exception;

/** Valid request, but it conflicts with current state (overlap, limit, wrong status, check-in window) -> 409. */
public class ReservationConflictException extends BusinessException {

    public ReservationConflictException(String message) {
        super(ErrorCode.RESERVATION_CONFLICT, message);
    }
}
