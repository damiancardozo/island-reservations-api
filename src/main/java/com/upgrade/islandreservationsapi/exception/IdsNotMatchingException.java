package com.upgrade.islandreservationsapi.exception;

public class IdsNotMatchingException extends Exception {

    public IdsNotMatchingException() {
        super("Ids don't match.");
    }
}
