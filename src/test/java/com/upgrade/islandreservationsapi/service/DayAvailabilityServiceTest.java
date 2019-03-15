package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.dto.DayAvailability;
import com.upgrade.islandreservationsapi.dto.Occupancy;
import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.model.Dates;
import com.upgrade.islandreservationsapi.repository.DatesRepository;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
public class DayAvailabilityServiceTest {

    @TestConfiguration
    static class DayAvailabilityServiceTestContextConfiguration {

        @Bean
        public DayAvailabilityServiceImpl reservationService() {
            return new DayAvailabilityServiceImpl();
        }
    }

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private ConfigurationService configurationService;

    @MockBean
    private DatesRepository datesRepository;

    @Autowired
    private DayAvailabilityService availabilityService;

    private static final int DEFAULT_MAX_AVAILABILITY = 100;
    private static final int DEFAULT_MAX_DATE_RANGE = 30;
    private static final int DEFAULT_MIN_AHEAD_DAYS = 1;
    private static final int DEFAULT_DEFAULT_DATE_RANGE = 30;

    @Before
    public void init() {
        Mockito.when(configurationService.getMaxAvailability()).thenReturn(DEFAULT_MAX_AVAILABILITY);
        Mockito.when(configurationService.getMaxDateRange()).thenReturn(DEFAULT_MAX_DATE_RANGE);
        Mockito.when(configurationService.getMinAheadDays()).thenReturn(DEFAULT_MIN_AHEAD_DAYS);
        Mockito.when(configurationService.getDefaultDateRange()).thenReturn(DEFAULT_DEFAULT_DATE_RANGE);

        List<Dates> dates = new ArrayList<>();
        LocalDate.now().datesUntil(LocalDate.now().plusDays(30)).forEach(d -> dates.add(new Dates(d)));
        Mockito.when(datesRepository.findAllById(any())).thenReturn(dates);
    }


    @Test(expected = InvalidDatesException.class)
    public void testGetAvailabilityInvalidDates() throws InvalidDatesException {
        LocalDate fromDate = LocalDate.now().plusDays(3);
        LocalDate toDate = LocalDate.now().plusDays(2);

        availabilityService.getAvailabilities(fromDate, toDate);
    }

    @Test(expected = InvalidDatesException.class)
    public void testGetAvailabilityInvalidDates2() throws InvalidDatesException {
        LocalDate fromDate = LocalDate.now().plusDays(DEFAULT_MIN_AHEAD_DAYS);
        LocalDate toDate = fromDate.plusDays(DEFAULT_MAX_DATE_RANGE);

        availabilityService.getAvailabilities(fromDate, toDate);
    }

    @Test
    public void testGetAvailabilityValidDates() throws InvalidDatesException {
        LocalDate fromDate = LocalDate.now().plusDays(DEFAULT_MIN_AHEAD_DAYS);
        LocalDate toDate = fromDate.plusDays(DEFAULT_MAX_DATE_RANGE - 1);

        availabilityService.getAvailabilities(fromDate, toDate);
    }

    @Test
    public void testGetAvailabilityNoRecords() throws InvalidDatesException {
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(10);

        Mockito.when(reservationRepository.calculateOccupancyByRange(fromDate, toDate)).thenReturn(new ArrayList<>());

        List<DayAvailability> avalabilities = availabilityService.getAvailabilities(fromDate, toDate);

        assertNotNull(avalabilities);
        assertEquals(10, avalabilities.size());

        for(int i = 0; i <= 9; i++) {
            DayAvailability availability = avalabilities.get(i);
            LocalDate date = LocalDate.now().plusDays(i + 1);
            assertEquals(date, availability.getDate());
            assertEquals(DEFAULT_MAX_AVAILABILITY, availability.getAvailability());
        }
    }

    @Test
    public void testGetAvailability() throws InvalidDatesException {
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(10);

        Occupancy o1 = new Occupancy(LocalDate.now().plusDays(4), 10);
        Occupancy o2 = new Occupancy(LocalDate.now().plusDays(5), 10);
        Occupancy o3 = new Occupancy(LocalDate.now().plusDays(7), 15);
        Occupancy o4 = new Occupancy(LocalDate.now().plusDays(8), 20);
        List<Occupancy> occupancies = List.of(o1, o2, o3, o4);

        Mockito.when(reservationRepository.calculateOccupancyByRange(fromDate, toDate)).thenReturn(occupancies);
        Mockito.when(configurationService.getMaxAvailability()).thenReturn(DEFAULT_MAX_AVAILABILITY);

        List<DayAvailability> avalabilities = availabilityService.getAvailabilities(fromDate, toDate);

        assertNotNull(avalabilities);
        assertEquals(10, avalabilities.size());

        for(int i = 0; i <= 2; i++) {
            DayAvailability availability = avalabilities.get(i);
            LocalDate date = LocalDate.now().plusDays(i + 1);
            assertEquals(date, availability.getDate());
            assertEquals(DEFAULT_MAX_AVAILABILITY, availability.getAvailability());
        }
        DayAvailability availability1 = avalabilities.get(3);
        assertEquals(90, availability1.getAvailability());

        DayAvailability availability2 = avalabilities.get(4);
        assertEquals(90, availability2.getAvailability());

        DayAvailability availability3 = avalabilities.get(5);
        assertEquals(DEFAULT_MAX_AVAILABILITY, availability3.getAvailability());

        DayAvailability availability4 = avalabilities.get(6);
        assertEquals(85, availability4.getAvailability());

        DayAvailability availability5 = avalabilities.get(7);
        assertEquals(80, availability5.getAvailability());
    }

}
