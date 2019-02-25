package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.model.DayAvailability;

import java.time.LocalDate;
import java.util.List;


public interface DayAvailabilityService {

    List<DayAvailability> getAvailabilities(LocalDate fromDate, LocalDate toDate);

}
