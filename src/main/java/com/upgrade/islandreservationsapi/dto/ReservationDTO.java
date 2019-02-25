package com.upgrade.islandreservationsapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.validator.ReservationDates;
import javax.validation.constraints.*;
import java.time.LocalDate;

@ReservationDates(startDateField = "start", endDateField = "end")
public class ReservationDTO {

    @NotBlank()
    private String fistName;
    @NotBlank
    private String lastName;
    @NotBlank
    @Email
    private String email;
    @NotNull
    @Future
    @JsonFormat(pattern="yyyy/MM/dd")
    private LocalDate start;
    @NotNull
    @JsonFormat(pattern="yyyy/MM/dd")
    private LocalDate end;
    @NotNull
    @Positive
    private Integer numberOfPersons;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Reservation.Status status;

    public String getFistName() {
        return fistName;
    }

    public void setFistName(String fistName) {
        this.fistName = fistName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public Integer getNumberOfPersons() {
        return numberOfPersons;
    }

    public void setNumberOfPersons(Integer numberOfPersons) {
        this.numberOfPersons = numberOfPersons;
    }

    public Reservation.Status getStatus() {
        return status;
    }

    public void setStatus(Reservation.Status status) {
        this.status = status;
    }
}
