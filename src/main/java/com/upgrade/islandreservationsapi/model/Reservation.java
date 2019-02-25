package com.upgrade.islandreservationsapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.upgrade.islandreservationsapi.validator.ReservationDates;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "Reservation")
@ReservationDates(startDateField = "start", endDateField = "end")
public class Reservation {

    public enum Status {
        @JsonProperty("Active")
        ACTIVE,
        @JsonProperty("Cancelled")
        CANCELLED
    }

    @Column(name = "ReservationID")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "FirstName")
    @NotBlank()
    private String fistName;
    @Column(name = "LastName")
    @NotBlank
    private String lastName;
    @Column(name = "Email")
    @NotBlank
    @Email
    private String email;
    @Column(name = "Start")
    @NotNull
    @Future()
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate start;
    @Column(name = "End")
    @NotNull
    @Future(message = "Must be a future date")
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate end;
    @Column(name = "NumberOfPersons")
    @NotNull
    @Positive
    private Integer numberOfPersons;
    @Column(name = "Status")
    @Enumerated(EnumType.STRING)
    private Status status;

    public Reservation() {}

    public Reservation(@NotNull String fistName, @NotNull String lastName, @NotNull @Email String email,
                       @NotNull LocalDate start, @NotNull LocalDate end, @NotNull Integer numberOfPersons) {
        this.fistName = fistName;
        this.lastName = lastName;
        this.email = email;
        this.start = start;
        this.end = end;
        this.numberOfPersons = numberOfPersons;
        this.status = Status.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", fistName='" + fistName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", numberOfPersons=" + numberOfPersons +
                ", status=" + status +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
