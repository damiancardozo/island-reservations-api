package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.dto.DayAvailability;
import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
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

}
