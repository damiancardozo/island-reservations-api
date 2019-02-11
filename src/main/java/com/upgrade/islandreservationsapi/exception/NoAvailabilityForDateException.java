package com.upgrade.islandreservationsapi.exception;

public class NoAvailabilityForDateException extends Exception {

    public NoAvailabilityForDateException() {
        super("There's no availability for the date period.");
    }

}
