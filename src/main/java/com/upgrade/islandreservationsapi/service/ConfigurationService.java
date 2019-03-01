package com.upgrade.islandreservationsapi.service;


public interface ConfigurationService {

    /**
     * Returns the max availability for any date.
     * If this configuration record does not exist, inserts a new one with a default value
     * and returns it
     * @return int with max availability
     */
    int getMaxAvailability();

    /**
     * Returns the max allowed duration for a reservation (number of days)
     * If this configuration record does not exist, inserts a new one with a default value
     * @return int with max allowed duration
     */
    int getMaxReservation();

    /**
     * Returns the minimum number of days in ahead a reservation must be created/updated (number of days)
     * If this configuration record does not exist, inserts a new one with a default value
     * @return int with minimum number of days ahead for creating/updating a reservation
     */
    int getMinAheadDays();

    /**
     * Returns the max number of days in ahead a reservation must be created (number of days)
     * If this configuration record does not exist, inserts a new one with a default value
     * @return int with max number of days ahead for creating/updating a reservation
     */
    int getMaxAheadDays();

    /**
     * Gets the max number of days that it is allowed to check for availability
     * If this configuration record does not exist, inserts a new one with a default value
     * @return int with max date range for availability check
     */
    int getMaxDateRange();
}
