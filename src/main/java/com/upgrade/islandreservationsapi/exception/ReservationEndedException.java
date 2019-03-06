package com.upgrade.islandreservationsapi.exception;

public class ReservationEndedException extends InvalidReservationException {

    public ReservationEndedException() {
        super("Reservation ended. Updates are not allowed.");
    }
}
