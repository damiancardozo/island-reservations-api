package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.DayAvailability;

public interface DayAvailabilityRepositoryCustom {

    void refresh(DayAvailability da);
}
