package com.upgrade.islandreservationsapi.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "day_availability")
public class DayAvailability {

    @Id
    @NotNull
    private LocalDate date;
    private int availability;

    public DayAvailability() {}

    public DayAvailability(LocalDate date, int availability) {
        this.date = date;
        this.availability = availability;
    }

    @Override
    public String toString() {
        return "DayAvailability{" +
                "date=" + date +
                ", availability=" + availability +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DayAvailability that = (DayAvailability) o;
        return availability == that.availability &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, availability);
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
