package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.exception.NoAvailabilityForDateException;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.DayAvailabilityRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DayAvailabilityServiceImpl implements DayAvailabilityService {

    @Autowired
    private DayAvailabilityRepository availabilityRepository;

    @Autowired
    private ConfigurationService configurationService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Logger logger = LogManager.getLogger(DayAvailabilityServiceImpl.class);

    @Override
    public List<DayAvailability> getAvailabilities(LocalDate fromDate, LocalDate toDate) throws InvalidDatesException {

        logger.info("getAvailabilities(): Checking availability for dates {}-{}.",
                fromDate.format(formatter), toDate.format(formatter));
        if(fromDate.isAfter(toDate)) {
            logger.info("getAvailabilities(): Invalid dates. toDate must be after fromDate.");
            throw new InvalidDatesException("toDate must be after fromDate.");
        }
        int maxDateRange = configurationService.getMaxDateRange();
        if(ChronoUnit.DAYS.between(fromDate, toDate) > maxDateRange) {
            logger.info("getAvailabilities(): Invalid dates. Date range is too long.");
            throw new InvalidDatesException("Date range is too long. Please send a date range of " + maxDateRange + " days max.");
        }
        if(!fromDate.isAfter(LocalDate.now())) {
            logger.info("getAvailabilities(): Invalid dates. fromDate must be at least tomorrow.");
            throw new InvalidDatesException("Can only check availability starting tomorrow.");
        }

        int maxAvailability = configurationService.getMaxAvailability();

        List<DayAvailability> availabilities = availabilityRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate);
        LocalDate date = LocalDate.from(fromDate);
        List<DayAvailability> allAvailabilities = new ArrayList<>();
        if(availabilities.isEmpty()) {
            logger.info("No DayAvailability records found in database for period {}-{}. " +
                    "Will return max availability for all these dates.", fromDate.format(formatter), toDate.format(formatter));
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

    public List<DayAvailability> updateDayAvailability(Reservation reservation)
            throws NoAvailabilityForDateException {
        List<LocalDate> dates = reservation.getStart().datesUntil(reservation.getEnd()).collect(Collectors.toList());
        List<DayAvailability> availabilities = availabilityRepository.findAllById(dates);
        Map<LocalDate, DayAvailability> availabilityMap = availabilities.stream()
                .collect(Collectors.toMap(DayAvailability::getDate, v -> v));
        int maxOccupancy = configurationService.getMaxAvailability();
        for(LocalDate date: dates) {
            Optional<DayAvailability> availabilityOpt = Optional.ofNullable(availabilityMap.get(date));
            DayAvailability dayAvailability = availabilityOpt.orElse(new DayAvailability(date, maxOccupancy, maxOccupancy));
            dayAvailability.setAvailability(dayAvailability.getAvailability() - reservation.getNumberOfPersons());
            if(dayAvailability.getAvailability() < 0) {
                throw new NoAvailabilityForDateException();
            }
            availabilities.add(dayAvailability);
        }
        return availabilityRepository.saveAll(availabilities);
    }

    public void addAvailability(LocalDate fromDate, LocalDate toDate, int number) {
        logger.info("Adding {} to the availability to all DayAvailability records between {} and {}.",
                number, fromDate.format(formatter), toDate.format(formatter));
        List<LocalDate> dates = fromDate.datesUntil(toDate).collect(Collectors.toList());
        List<DayAvailability> availabilities = availabilityRepository.findAllById(dates);
        for(DayAvailability availability: availabilities) {
            availability.setAvailability(availability.getAvailability() + number);
        }
        availabilityRepository.saveAll(availabilities);
    }
}
