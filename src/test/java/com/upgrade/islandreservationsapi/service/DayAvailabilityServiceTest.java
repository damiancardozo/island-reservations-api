package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.repository.DayAvailabilityRepository;
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
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    private DayAvailabilityRepository availabilityRepository;

    @MockBean
    private ConfigurationService configurationService;

    @Autowired
    private DayAvailabilityService availabilityService;

    private static final int DEFAULT_MAX_AVAILABILITY = 100;
    private static final int DEFAULT_MAX_DATE_RANGE = 30;

    @Before
    public void init() {
        Mockito.when(configurationService.getMaxAvailability()).thenReturn(DEFAULT_MAX_AVAILABILITY);
        Mockito.when(configurationService.getMaxDateRange()).thenReturn(DEFAULT_MAX_DATE_RANGE);
    }


    @Test(expected = InvalidDatesException.class)
    public void testGetAvailabilityInvalidDates() throws InvalidDatesException {
        LocalDate fromDate = LocalDate.now().plusDays(3);
        LocalDate toDate = LocalDate.now().plusDays(2);

        availabilityService.getAvailabilities(fromDate, toDate);
    }

    @Test
    public void testGetAvailabilityNoRecords() throws InvalidDatesException {
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(10);

        Mockito.when(availabilityRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate)).thenReturn(Collections.emptyList());

        List<DayAvailability> avalabilities = availabilityService.getAvailabilities(fromDate, toDate);

        assertNotNull(avalabilities);
        assertEquals(10, avalabilities.size());

        for(int i = 0; i <= 9; i++) {
            DayAvailability availability = avalabilities.get(i);
            LocalDate date = LocalDate.now().plusDays(i + 1);
            assertEquals(date, availability.getDate());
            assertEquals(DEFAULT_MAX_AVAILABILITY, availability.getAvailability());
            assertEquals(DEFAULT_MAX_AVAILABILITY, availability.getMaxAvailability());
        }
    }

    @Test
    public void testGetAvailability() throws InvalidDatesException {
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(10);

        DayAvailability a1 = new DayAvailability(LocalDate.now().plusDays(4), 90, DEFAULT_MAX_AVAILABILITY);
        DayAvailability a2 = new DayAvailability(LocalDate.now().plusDays(5), 90, DEFAULT_MAX_AVAILABILITY);
        DayAvailability a3 = new DayAvailability(LocalDate.now().plusDays(7), 85, DEFAULT_MAX_AVAILABILITY);
        DayAvailability a4 = new DayAvailability(LocalDate.now().plusDays(8), 80, DEFAULT_MAX_AVAILABILITY);

        List<DayAvailability> availabilitiesInDb = List.of(a1, a2, a3, a4);

        Mockito.when(availabilityRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate)).thenReturn(availabilitiesInDb);
        Mockito.when(configurationService.getMaxAvailability()).thenReturn(DEFAULT_MAX_AVAILABILITY);

        List<DayAvailability> avalabilities = availabilityService.getAvailabilities(fromDate, toDate);

        assertNotNull(avalabilities);
        assertEquals(10, avalabilities.size());

        for(int i = 0; i <= 2; i++) {
            DayAvailability availability = avalabilities.get(i);
            LocalDate date = LocalDate.now().plusDays(i + 1);
            assertEquals(date, availability.getDate());
            assertEquals(DEFAULT_MAX_AVAILABILITY, availability.getAvailability());
            assertEquals(DEFAULT_MAX_AVAILABILITY, availability.getMaxAvailability());
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
