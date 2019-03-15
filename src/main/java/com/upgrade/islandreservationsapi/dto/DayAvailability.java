package com.upgrade.islandreservationsapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class DayAvailability {

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate date;
    private int availability;

    public DayAvailability() {}

    public DayAvailability(LocalDate date, int availability) {
        this.date = date;
        this.availability = availability;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }
}
