package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.Reservation;
import org.springframework.stereotype.Service;

@Service
public interface ReservationService {

    Reservation getReservation(Integer id) throws ReservationNotFoundException;

    Reservation createReservation(Reservation reservation)
            throws NoAvailabilityForDateException, DayAvailabilityNotFoundException;

    Reservation updateReservation(Reservation reservation)
            throws NoAvailabilityForDateException, DayAvailabilityNotFoundException, ReservationNotFoundException;

    Reservation cancelReservation(Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException, NoAvailabilityForDateException, DayAvailabilityNotFoundException;
}
