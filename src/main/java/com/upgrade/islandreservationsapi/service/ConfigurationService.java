package com.upgrade.islandreservationsapi.service;

import org.springframework.stereotype.Service;

@Service
public interface ConfigurationService {

    int getMaxAvailability();

    int getMaxReservation();

    int getMinAheadDays();

    int getMaxAheadDays();
}
