package com.upgrade.islandreservationsapi.controller;

import com.upgrade.islandreservationsapi.dto.CreateReservationDTO;
import com.upgrade.islandreservationsapi.dto.ReservationCreated;
import com.upgrade.islandreservationsapi.dto.ReservationDTO;
import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.service.ReservationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Api(value = "reservations")
public class ReservationController {

    @Autowired
    private ReservationService service;

    @GetMapping(path = "v1/reservations/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    @ApiOperation(value = "Read a reservation by its ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reservation returned"),
            @ApiResponse(code = 404, message = "Reservation not found")
    })
    public ReservationDTO getReservation(@PathVariable Integer id) throws ReservationNotFoundException {
        Reservation reservation = service.getReservation(id);
        return new ModelMapper().map(reservation, ReservationDTO.class);
    }

    @PostMapping(path = "v1/reservations", produces = "application/json; charset=utf-8")
    @ResponseBody
    @ApiOperation(value = "Create a new reservation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reservation created successfully"),
            @ApiResponse(code = 400, message = "There's no availability, or validation error")
    })
    public ReservationCreated createReservation(@Valid @RequestBody CreateReservationDTO reservationDto)
            throws NoAvailabilityForDateException {
        Reservation reservation = new ModelMapper().map(reservationDto, Reservation.class);
        reservation = service.createReservation(reservation);
        return new ReservationCreated(reservation.getId());
    }

    @PutMapping(path = "v1/reservations/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    @ApiOperation(value = "Update an existing reservation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reservation updated successfully"),
            @ApiResponse(code = 400, message = "Reservation is cancelled; or there's no availability, or validation error"),
            @ApiResponse(code = 404, message = "Reservation not found")
    })
    public ReservationDTO updateReservation(@PathVariable Integer id, @Valid @RequestBody ReservationDTO reservationDto)
            throws NoAvailabilityForDateException, ReservationNotFoundException, StatusChangeNotAllowedException, ReservationCancelledException {
        ModelMapper mapper = new ModelMapper();
        Reservation reservation = mapper.map(reservationDto, Reservation.class);
        reservation.setId(id);
        if(reservation.getStatus() == null) {
            reservation.setStatus(Reservation.Status.ACTIVE);
        }
        reservation = service.updateReservation(reservation);
        return mapper.map(reservation, ReservationDTO.class);
    }

    @DeleteMapping(path = "v1/reservations/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    @ApiOperation(value = "Cancel an active reservation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reservation cancelled successfully"),
            @ApiResponse(code = 400, message = "Reservation is already cancelled"),
            @ApiResponse(code = 404, message = "Reservation not found")
    })
    public ReservationDTO cancelReservation(@PathVariable Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException {
        Reservation reservation = service.cancelReservation(id);
        ModelMapper mapper = new ModelMapper();
        return mapper.map(reservation, ReservationDTO.class);
    }
}
