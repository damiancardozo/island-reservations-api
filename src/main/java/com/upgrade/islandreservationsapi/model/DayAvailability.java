package com.upgrade.islandreservationsapi.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "DayAvailability")
public class DayAvailability {

    @Column(name = "Date")
    @Id
    @NotNull
    private LocalDate date;
    @Column(name = "Availability")
    private int availability;
    @Column(name = "MaxAvailability")
    private int maxAvailability;

    public DayAvailability() {}

    public DayAvailability(LocalDate date, int availability, int maxAvailability) {
        this.date = date;
        this.availability = availability;
        this.maxAvailability = maxAvailability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DayAvailability that = (DayAvailability) o;
        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    @Override
    public String toString() {
        return "DayAvailability{" +
                "date=" + date +
                ", availability=" + availability +
                ", maxAvailability=" + maxAvailability +
                '}';
    }

    public int getMaxAvailability() {
        return maxAvailability;
    }

    public void setMaxAvailability(int maxAvailability) {
        this.maxAvailability = maxAvailability;
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
