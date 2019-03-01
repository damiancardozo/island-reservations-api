package com.upgrade.islandreservationsapi.exception;

public class ReservationCancelledException extends Exception{

    public ReservationCancelledException() {
        super("Reservation is cancelled. Please create a new one.");
    }
}
