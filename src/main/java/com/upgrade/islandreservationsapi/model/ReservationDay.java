package com.upgrade.islandreservationsapi.model;

import javax.persistence.*;

@Entity
@Table(name = "ReservationDay")
public class ReservationDay {

    @EmbeddedId
    private ReservationDayIdentity id;
    private Integer numberOfPersons;

    public ReservationDay() {
    }

    public ReservationDay(ReservationDayIdentity id, Integer numberOfPersons) {
        this.id = id;
        this.numberOfPersons = numberOfPersons;
    }

    public Integer getNumberOfPersons() {
        return numberOfPersons;
    }

    public void setNumberOfPersons(Integer numberOfPersons) {
        this.numberOfPersons = numberOfPersons;
    }

    public ReservationDayIdentity getId() {
        return id;
    }

    public void setId(ReservationDayIdentity id) {
        this.id = id;
    }
}
