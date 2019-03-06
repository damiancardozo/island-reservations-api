package com.upgrade.islandreservationsapi.exception;

public class StatusChangeNotAllowedException extends InvalidReservationException {

    public StatusChangeNotAllowedException() {
        super("Status change is not allowed with this method.");
    }

    public StatusChangeNotAllowedException(String message) {
        super(message);
    }
}
