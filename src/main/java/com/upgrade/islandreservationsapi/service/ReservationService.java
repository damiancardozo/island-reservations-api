package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.Reservation;

public interface ReservationService {

    Reservation getReservation(Integer id) throws ReservationNotFoundException;

    Reservation createReservation(Reservation reservation)
            throws NoAvailabilityForDateException;

    Reservation updateReservation(Reservation reservation)
            throws NoAvailabilityForDateException, ReservationNotFoundException;

    Reservation cancelReservation(Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException;
}
