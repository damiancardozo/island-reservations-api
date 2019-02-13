package com.upgrade.islandreservationsapi.dto;

public class ReservationCreated {

    private Integer id;

    public ReservationCreated() {}

    public ReservationCreated(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
