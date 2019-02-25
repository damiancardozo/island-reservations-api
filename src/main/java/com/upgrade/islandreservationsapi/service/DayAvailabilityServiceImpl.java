package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.repository.DayAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DayAvailabilityServiceImpl implements DayAvailabilityService {

    @Autowired
    private DayAvailabilityRepository availabilityRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public List<DayAvailability> getAvailabilities(LocalDate fromDate, LocalDate toDate) throws InvalidDatesException {

        if(fromDate.isAfter(toDate)) {
            throw new InvalidDatesException("toDate must be after fromDate.");
        }
        int maxDateRange = configurationService.getMaxDateRange();
        if(ChronoUnit.DAYS.between(fromDate, toDate) > 180) {
            throw new InvalidDatesException("Date range is too long. Please send a date range of " + maxDateRange + " days max.");
        }
        if(!fromDate.isAfter(LocalDate.now())) {
            throw new InvalidDatesException("Can only check availibility starting tomorrow.");
        }

        int maxAvailability = configurationService.getMaxAvailability();

        List<DayAvailability> availabilities = availabilityRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate);
        LocalDate date = LocalDate.from(fromDate);
        List<DayAvailability> allAvailabilities = new ArrayList<>();
        if(availabilities.isEmpty()) {
            fromDate.datesUntil(toDate.plusDays(1))
                    .forEach(d -> allAvailabilities.add(new DayAvailability(d, maxAvailability, maxAvailability)));
            date = toDate;
        }
        for(DayAvailability availability: availabilities) {
            if(availability.getDate().isAfter(date)) {
                date.datesUntil(availability.getDate())
                        .forEach(d -> allAvailabilities.add(new DayAvailability(d, maxAvailability, maxAvailability)));
            }
            allAvailabilities.add(availability);
            date = availability.getDate().plusDays(1);
        }
        if(toDate.isAfter(date)) {
            date.datesUntil(toDate.plusDays(1))
                    .forEach(d -> allAvailabilities.add(new DayAvailability(d, maxAvailability, maxAvailability)));
        }
        return allAvailabilities;
    }
}
