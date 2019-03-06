package com.upgrade.islandreservationsapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class DayAvailabilityDTO {

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate date;
    private int availability;

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
