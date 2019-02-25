package com.upgrade.islandreservationsapi.controller;

import com.upgrade.islandreservationsapi.dto.CreateReservationDTO;
import com.upgrade.islandreservationsapi.dto.ReservationCreated;
import com.upgrade.islandreservationsapi.dto.ReservationDTO;
import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.service.ReservationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class ReservationController {

    @Autowired
    private ReservationService service;

    @GetMapping("v1/reservations/{id}")
    @ResponseBody
    public Reservation getReservation(@PathVariable Integer id) throws ReservationNotFoundException {
        return service.getReservation(id);
    }

    @PostMapping("v1/reservations")
    @ResponseBody
    public ReservationCreated createReservation(@Valid @RequestBody CreateReservationDTO reservationDto)
            throws NoAvailabilityForDateException {
        Reservation reservation = new ModelMapper().map(reservationDto, Reservation.class);
        reservation = service.createReservation(reservation);
        return new ReservationCreated(reservation.getId());
    }

    @PutMapping("v1/reservations/{id}")
    @ResponseBody
    public ReservationDTO updateReservation(@PathVariable Integer id, @Valid @RequestBody ReservationDTO reservationDto)
            throws NoAvailabilityForDateException, ReservationNotFoundException {
        ModelMapper mapper = new ModelMapper();
        Reservation reservation = mapper.map(reservationDto, Reservation.class);
        reservation.setId(id);
        reservation = service.updateReservation(reservation);
        return mapper.map(reservation, ReservationDTO.class);
    }

    @DeleteMapping("v1/reservations/{id}")
    @ResponseBody
    public ReservationDTO cancelReservation(@PathVariable Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException {
        Reservation reservation = service.cancelReservation(id);
        ModelMapper mapper = new ModelMapper();
        return mapper.map(reservation, ReservationDTO.class);
    }
}
