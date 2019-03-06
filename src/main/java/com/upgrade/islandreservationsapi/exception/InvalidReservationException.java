package com.upgrade.islandreservationsapi.exception;

import com.upgrade.islandreservationsapi.dto.ApiFieldError;

import java.util.ArrayList;
import java.util.List;

public class InvalidReservationException extends Exception {

    private final ArrayList<ApiFieldError> errors;

    InvalidReservationException(String message) {
        super(message);
        errors = null;
    }

    public InvalidReservationException(String message, List<ApiFieldError> errors) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.addAll(errors);
    }

    public List<ApiFieldError> getErrors() {
        return errors;
    }

}
