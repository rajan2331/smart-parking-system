package com.parking.common.exception;

public class ParkingSessionAlreadyCompletedException
extends BusinessException {

public ParkingSessionAlreadyCompletedException(String message) {
super(ErrorCode.PARKING_SESSION_ALREADY_COMPLETED, message);
}
}