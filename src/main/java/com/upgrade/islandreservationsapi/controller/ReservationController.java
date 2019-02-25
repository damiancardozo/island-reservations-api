package com.upgrade.islandreservationsapi.controller;

import com.upgrade.islandreservationsapi.dto.ReservationCreated;
import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService service;

    @GetMapping("/{id}")
    @ResponseBody
    public Reservation getReservation(@PathVariable Integer id) throws ReservationNotFoundException {
        return service.getReservation(id);
    }

    @PostMapping
    @ResponseBody
    public ReservationCreated createReservation(@Valid @RequestBody Reservation reservation)
            throws NoAvailabilityForDateException {
        Reservation newReservation = service.createReservation(reservation);
        return new ReservationCreated(newReservation.getId());
    }

    @PutMapping("/{id}")
    @ResponseBody
    public Reservation updateReservation(@PathVariable Integer id, @Valid @RequestBody Reservation reservation)
            throws NoAvailabilityForDateException, IdsNotMatchingException, ReservationNotFoundException {
        if(reservation.getId() != null && !reservation.getId().equals(id)) {
            throw new IdsNotMatchingException();
        } else if(reservation.getId() == null) {
            reservation.setId(id);
        }
        return service.updateReservation(reservation);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public Reservation cancelReservation(@PathVariable Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException {
        return service.cancelReservation(id);
    }
}
