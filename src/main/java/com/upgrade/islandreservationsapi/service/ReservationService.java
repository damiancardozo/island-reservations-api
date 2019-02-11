package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.NoAvailabilityForDateException;
import com.upgrade.islandreservationsapi.exception.ReservationAlreadyCancelledException;
import com.upgrade.islandreservationsapi.exception.ReservationNotFoundException;
import com.upgrade.islandreservationsapi.model.Reservation;

public interface ReservationService {

    Reservation getReservation(Integer id) throws ReservationNotFoundException;

    Reservation createReservation(Reservation reservation) throws NoAvailabilityForDateException;

    Reservation updateReservation(Reservation reservation) throws NoAvailabilityForDateException;

    Reservation cancelReservation(Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException;
}
