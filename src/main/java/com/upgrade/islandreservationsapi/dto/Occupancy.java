package com.upgrade.islandreservationsapi.dto;


import java.math.BigInteger;
import java.time.ZoneId;
import java.util.Date;
import java.time.LocalDate;

public class Occupancy {

    private LocalDate date;
    private Integer value;

    public Occupancy() {}

    public Occupancy(Date date, BigInteger value) {
        this.date = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.value = value.intValue();
    }

    public Occupancy(LocalDate date, long value) {
        this.date = date;
        this.value = (int) value;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
