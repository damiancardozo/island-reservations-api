package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.dto.DayAvailability;
import com.upgrade.islandreservationsapi.dto.Occupancy;
import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.repository.DatesRepository;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DayAvailabilityServiceImpl implements DayAvailabilityService {

    @Autowired
    private DatesRepository datesRepository;

    @Autowired
    private ReservationRepository reservationRepository;

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
        int range = (int) ChronoUnit.DAYS.between(fromDate, toDate.plusDays(1));
        if(range > maxDateRange) {
            logger.info("getAvailabilities(): Invalid dates. Date range {} is greater than the max {}", range, maxDateRange);
            throw new InvalidDatesException("Date range is too long. Please send a date range of " + maxDateRange + " days max.");
        }
        if(!fromDate.isAfter(LocalDate.now())) {
            logger.info("getAvailabilities(): Invalid dates. fromDate must be at least tomorrow.");
            throw new InvalidDatesException("Can only check availability starting tomorrow.");
        }

        final int maxAvailability = configurationService.getMaxAvailability();

        final List<Occupancy> occupancies = reservationRepository.calculateOccupancyByRange(fromDate, toDate);
        LocalDate date = LocalDate.from(fromDate);
        final List<DayAvailability> allAvailabilities = new ArrayList<>();
        if(occupancies.isEmpty()) {
            logger.info("No DayAvailability records found in database for period {}-{}. " +
                    "Will return max availability for all these dates.", fromDate.format(formatter), toDate.format(formatter));
            fromDate.datesUntil(toDate.plusDays(1))
                    .forEach(d -> allAvailabilities.add(new DayAvailability(d, maxAvailability)));
            date = toDate;
        }
        for(Occupancy occupancy: occupancies) {
            if(occupancy.getDate().isAfter(date)) {
                date.datesUntil(occupancy.getDate())
                        .forEach(d -> allAvailabilities.add(new DayAvailability(d, maxAvailability)));
            }
            allAvailabilities.add(new DayAvailability(occupancy.getDate(), maxAvailability - occupancy.getValue()));
            date = occupancy.getDate().plusDays(1);
        }
        if(toDate.isAfter(date)) {
            date.datesUntil(toDate.plusDays(1))
                    .forEach(d -> allAvailabilities.add(new DayAvailability(d, maxAvailability)));
        }
        return allAvailabilities;
    }

}
