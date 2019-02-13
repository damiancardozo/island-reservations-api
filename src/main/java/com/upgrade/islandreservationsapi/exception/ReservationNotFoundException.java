package com.upgrade.islandreservationsapi.exception;

public class ReservationNotFoundException extends Exception {

    public ReservationNotFoundException() {
        super("Reservation not found");
    }

    public ReservationNotFoundException(Integer id) {
        super(String.format("Reservation with id %d not found", id));
    }

}
