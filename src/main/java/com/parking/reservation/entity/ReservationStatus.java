package com.parking.reservation.entity;

public enum ReservationStatus {
    /** Booked; the spot is held for the window. */
    CONFIRMED,
    /** Driver arrived; a parking session was started from this reservation. */
    CHECKED_IN,
    /** Cancelled by the driver before check-in. */
    CANCELLED,
    /** No-show: not checked in within the grace period after the start time. */
    EXPIRED
}
