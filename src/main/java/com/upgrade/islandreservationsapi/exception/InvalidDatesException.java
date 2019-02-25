package com.upgrade.islandreservationsapi.exception;

public class InvalidDatesException extends Exception {

    public InvalidDatesException() {
        super("");
    }

    public InvalidDatesException(String message) {
        super(message);
    }
}
