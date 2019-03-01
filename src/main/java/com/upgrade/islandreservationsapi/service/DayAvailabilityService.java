package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.exception.NoAvailabilityForDateException;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.model.Reservation;

import java.time.LocalDate;
import java.util.List;


public interface DayAvailabilityService {

    /**
     * Get DayAvailabilities between the provided date range. If a DayAvailability does not exist
     * for any of the dates within the range a new DayAvailability is created (but not stored in DB).
     * @param fromDate fromDate (inclusive)
     * @param toDate toDate (inclusive)
     * @return List of DayAvailability
     * @throws InvalidDatesException if fromDate > toDate, if date range is greater than the maximum allowed,
     * if fromDate is before tomorrow.
     */
    List<DayAvailability> getAvailabilities(LocalDate fromDate, LocalDate toDate) throws InvalidDatesException;

    /**
     * Based on reservation dates, updates existing DayAvailability records (or creates new ones) substracting
     * the number of persons in the reservation to the availability.
     * @param reservation Reservation based on which day availability should be updated
     * @return List of DayAvailability for the reservation dates: start (inclusive), end (exclusive)
     * @throws NoAvailabilityForDateException if there's no availability any of the dates to accomodate the
     * number of persons in the reservation.
     */
    List<DayAvailability> updateDayAvailability(Reservation reservation)
            throws NoAvailabilityForDateException;

    /**
     * Updates the availability of all DayAvailability records within the provided date range by adding the
     * provided number. To increase availability, use a positive number. To decrease availability, use a negative number.
     * if there's no record for any of the dates a new DayAvailability record is NOT created.
     * @param fromDate inclusive
     * @param toDate exclusive
     * @param number number to add to current availability for all DayAvailability records.
     */
    void addAvailability(LocalDate fromDate, LocalDate toDate, int number);

}
