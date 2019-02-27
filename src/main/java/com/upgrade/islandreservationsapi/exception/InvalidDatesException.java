package com.upgrade.islandreservationsapi.exception;

public class InvalidDatesException extends Exception {

    public InvalidDatesException() {
        super("Dates are invalid");
    }

    public InvalidDatesException(String message) {
        super(message);
    }
}
