package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.exception.NoAvailabilityForDateException;
import com.upgrade.islandreservationsapi.model.Dates;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.DatesRepository;
import com.upgrade.islandreservationsapi.repository.DayAvailabilityRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    private DatesRepository datesRepository;

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
        long maxDateRange = configurationService.getMaxDateRange();
        if(maxDateRange % 30 == 0) {
            // if max date range is 30, then treat it as a month. so update it based on this month's number of days
            maxDateRange = ChronoUnit.DAYS.between(fromDate, LocalDate.now().plusMonths(1).plusDays(1));
        }
        if(ChronoUnit.DAYS.between(fromDate, toDate) >= maxDateRange) {
            logger.info("getAvailabilities(): Invalid dates. Date range is too long.");
            throw new InvalidDatesException("Date range is too long. Please send a date range of " + maxDateRange + " days max.");
        }
        if(!fromDate.isAfter(LocalDate.now())) {
            logger.info("getAvailabilities(): Invalid dates. fromDate must be at least tomorrow.");
            throw new InvalidDatesException("Can only check availability starting tomorrow.");
        }

        final int maxAvailability = configurationService.getMaxAvailability();

        final List<DayAvailability> availabilities = availabilityRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate);
        LocalDate date = LocalDate.from(fromDate);
        final List<DayAvailability> allAvailabilities = new ArrayList<>();
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

    @Transactional(propagation = Propagation.REQUIRED)
    public List<DayAvailability> updateDayAvailability(Reservation reservation)
            throws NoAvailabilityForDateException {
        final List<LocalDate> dates = reservation.getStart().datesUntil(reservation.getEnd()).collect(Collectors.toList());
        logger.debug("updateDayAvailability(): finding dates by id: {}", dates);
        // lock dates
        final List<Dates> datesList = datesRepository.findAllById(dates);
        logger.debug("locked dates {}", datesList);
        logger.debug("updateDayAvailability(): finding availabilities by id: {}", dates);
        List<DayAvailability> availabilities = availabilityRepository.findAllById(dates);
        logger.debug("updateDayAvailability(): found {} DayAvailability records: {}", availabilities.size(), availabilities.toString());
        final Map<LocalDate, DayAvailability> availabilityMap = availabilities.stream()
                .collect(Collectors.toMap(DayAvailability::getDate, v -> v));
        final int maxOccupancy = configurationService.getMaxAvailability();
        for(LocalDate date: dates) {
            Optional<DayAvailability> availabilityOpt = Optional.ofNullable(availabilityMap.get(date));
            if(availabilityOpt.isEmpty()) {
                logger.debug("no availability record for date {}, creating with availability: {}",
                        date.format(formatter), maxOccupancy - reservation.getNumberOfPersons());
                DayAvailability newDayAvailability = new DayAvailability(date, maxOccupancy - reservation.getNumberOfPersons(), maxOccupancy);
                availabilities.add(newDayAvailability);
                availabilityRepository.save(newDayAvailability);
            } else {
                DayAvailability availability = availabilityOpt.get();
                if(availabilityOpt.get().getAvailability() - reservation.getNumberOfPersons() < 0) {
                    logger.info("no availability for date {}", date.format(formatter));
                    throw new NoAvailabilityForDateException();
                }
                logger.debug("updating availability for date {} from {} to {}",
                        date.format(formatter),
                        availability.getAvailability(),
                        availability.getAvailability() - reservation.getNumberOfPersons());
                availability.setAvailability(availability.getAvailability() - reservation.getNumberOfPersons());
                availabilityRepository.save(availability);
            }
        }
        return availabilities;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<DayAvailability> addAvailability(LocalDate fromDate, LocalDate toDate, int number) {
        logger.info("Adding {} to the availability to all DayAvailability records between {} and {}.",
                number, fromDate.format(formatter), toDate.format(formatter));
        final List<LocalDate> dates = fromDate.datesUntil(toDate).collect(Collectors.toList());
        // lock dates
        final List<Dates> allDates = datesRepository.findAllById(dates);
        logger.debug("locked dates {}", allDates);
        logger.debug("addAvailability(): finding dates by id: {}", dates);
        List<DayAvailability> availabilities = availabilityRepository.findAllById(dates);
        logger.debug("addAvailability(): found {} DayAvailability records: {}", availabilities.size(), availabilities.toString());
        for(DayAvailability availability: availabilities) {
            availabilityRepository.refresh(availability);
            availability.setAvailability(availability.getAvailability() + number);
            logger.debug("addAvailability(): set availability for date {} to {}",
                    availability.getDate().format(formatter), availability.getAvailability());
            availabilityRepository.save(availability);
        }
        return availabilities;
    }
}
