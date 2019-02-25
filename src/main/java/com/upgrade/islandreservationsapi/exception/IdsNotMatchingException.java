package com.upgrade.islandreservationsapi.exception;

public class IdsNotMatchingException extends Exception {

    public IdsNotMatchingException() {
        super("Id in the url does not match id in the request body.");
    }
}
