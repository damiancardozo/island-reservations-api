package com.upgrade.islandreservationsapi.dto;

public class ApiFieldError {

    private String path;
    private String message;

    public ApiFieldError() {}

    public ApiFieldError(String path, String message) {
        this.path = path;
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
