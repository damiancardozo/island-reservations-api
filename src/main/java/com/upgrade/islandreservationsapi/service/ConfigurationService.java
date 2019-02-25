package com.upgrade.islandreservationsapi.service;


public interface ConfigurationService {

    int getMaxAvailability();

    int getMaxReservation();

    int getMinAheadDays();

    int getMaxAheadDays();

    int getMaxDateRange();
}
