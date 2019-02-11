package com.upgrade.islandreservationsapi.exception;

public class ReservationAlreadyCancelledException extends Exception {

    public ReservationAlreadyCancelledException() {
        super("Reservation is already cancelled.");
    }

    public ReservationAlreadyCancelledException(Integer id) {
        super(String.format("Reservation with id %d is already cancelled.", id));
    }
}
