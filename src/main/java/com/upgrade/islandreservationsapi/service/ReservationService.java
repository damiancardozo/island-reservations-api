package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.Reservation;

public interface ReservationService {

    /**
     * Get a reservation from the database based on its ID.
     * @param id ID of reservation to read from DB
     * @return Reservation from DB
     * @throws ReservationNotFoundException if the reservation does not exist in the DB
     */
    Reservation getReservation(Integer id) throws ReservationNotFoundException;

    /**
     * Create a new reservation in the DB.
     * @param reservation Reservation to create
     * @return Created reservation
     * @throws NoAvailabilityForDateException if there's no availability to accomodate the number of persons
     * declared in the reservation for any of the reservation dates.
     */
    Reservation createReservation(Reservation reservation)
            throws NoAvailabilityForDateException;

    /**
     * Updates an existing reservation
     * @param reservation Reservation to update
     * @return Updated reservation
     * @throws NoAvailabilityForDateException if there's no availability to accomodate the number of persons
     *      * declared in the reservation for any of the reservation dates.
     * @throws ReservationNotFoundException if the reservation does not exist in the DB
     * @throws ReservationCancelledException if the reservation status is CANCELLED
     * @throws StatusChangeNotAllowedException if the new reservation status is different than the current status
     * of the reservation in the DB.
     */
    Reservation updateReservation(Reservation reservation)
            throws NoAvailabilityForDateException, ReservationNotFoundException, ReservationCancelledException, StatusChangeNotAllowedException;

    /**
     * Sets the status of a reservation to CANCELLED
     * @param id Id of the reservation to cancel.
     * @return Updated reservation
     * @throws ReservationNotFoundException if the reservation does not exist in the DB
     * @throws ReservationAlreadyCancelledException if the reservation status is already CANCELLED
     */
    Reservation cancelReservation(Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException;
}
