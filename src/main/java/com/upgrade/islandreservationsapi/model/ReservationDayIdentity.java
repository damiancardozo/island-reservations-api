package com.upgrade.islandreservationsapi.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class ReservationDayIdentity implements Serializable {

    private static final long serialVersionUID = -4988988998998292614L;

    @Column(name = "Date")
    private LocalDate date;
    @ManyToOne
    @JoinColumn(name = "ReservationFK")
    private Reservation reservation;

    public ReservationDayIdentity() {}

    public ReservationDayIdentity(@NotNull LocalDate date, @NotNull Reservation reservation) {
        this.date = date;
        this.reservation = reservation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationDayIdentity that = (ReservationDayIdentity) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(reservation, that.reservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, reservation);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }
}
