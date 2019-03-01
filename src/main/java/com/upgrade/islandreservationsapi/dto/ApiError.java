package com.upgrade.islandreservationsapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiError {

    private HttpStatus status;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ApiFieldError> fieldErrors;

    public ApiError(HttpStatus status, String message, List<ApiFieldError> fieldErrors) {
        super();
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public ApiError(HttpStatus status, String message) {
        super();
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ApiFieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<ApiFieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
