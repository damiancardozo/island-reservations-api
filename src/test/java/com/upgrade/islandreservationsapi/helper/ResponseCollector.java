package com.upgrade.islandreservationsapi.helper;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector {

    private List<String> responseBodies = new ArrayList<>();
    private List<Integer> responseStatuses = new ArrayList<>();

    public ResponseCollector() {}

    public void addResponseBody(String body) {
        responseBodies.add(body);
    }

    public void addResponseStatus(Integer status) {
        responseStatuses.add(status);
    }

    public long countStatusByCode(HttpStatus code) {
        return responseStatuses.stream().filter(i -> code.value() == i).count();
    }

    public List<String> getResponseBodies() {
        return responseBodies;
    }

    public void setResponseBodies(List<String> responseBodies) {
        this.responseBodies = responseBodies;
    }

    public List<Integer> getResponseStatuses() {
        return responseStatuses;
    }

    public void setResponseStatuses(List<Integer> responseStatuses) {
        this.responseStatuses = responseStatuses;
    }
}
