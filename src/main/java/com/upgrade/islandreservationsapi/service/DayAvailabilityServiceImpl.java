package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.model.DayAvailability;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class DayAvailabilityServiceImpl implements DayAvailabilityService {
    @Override
    public List<DayAvailability> getAvailabilities(LocalDate fromDate, LocalDate toDate) {
        return Collections.emptyList();
    }
}
