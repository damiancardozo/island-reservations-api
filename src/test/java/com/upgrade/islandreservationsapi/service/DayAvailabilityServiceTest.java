package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.exception.NoAvailabilityForDateException;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.model.Reservation;
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
import java.util.ArrayList;
import java.util.Arrays;
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

        Mockito.when(availabilityRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate)).thenReturn(new ArrayList<>());

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

    @Test
    public void testAddAvailability() {

        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate date2 = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);

        int add = 8;

        DayAvailability a1 = new DayAvailability(fromDate, 80, 100);
        DayAvailability a2 = new DayAvailability(fromDate, 70, 100);
        List<DayAvailability> availabilities = List.of(a1, a2);

        Mockito.when(availabilityRepository.findAllById(List.of(fromDate, date2))).thenReturn(availabilities);
        Mockito.when(availabilityRepository.saveAll(availabilities)).thenReturn(availabilities);

        List<DayAvailability> updatedAvailabilities = availabilityService.addAvailability(fromDate, toDate, add);

        assertEquals(2, updatedAvailabilities.size());
        assertEquals(88, updatedAvailabilities.get(0).getAvailability());
        assertEquals(78, updatedAvailabilities.get(1).getAvailability());
    }

    @Test
    public void testSubstractAvailability() {

        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate date2 = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);

        int add = -5;

        DayAvailability a1 = new DayAvailability(fromDate, 80, 100);
        DayAvailability a2 = new DayAvailability(fromDate, 70, 100);
        List<DayAvailability> availabilities = List.of(a1, a2);

        Mockito.when(availabilityRepository.findAllById(List.of(fromDate, date2))).thenReturn(availabilities);
        Mockito.when(availabilityRepository.saveAll(availabilities)).thenReturn(availabilities);

        List<DayAvailability> updatedAvailabilities = availabilityService.addAvailability(fromDate, toDate, add);

        assertEquals(2, updatedAvailabilities.size());
        assertEquals(75, updatedAvailabilities.get(0).getAvailability());
        assertEquals(65, updatedAvailabilities.get(1).getAvailability());
    }

    @Test
    public void testUpdateDayAvailabilityNoExistingRecords() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate middleDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);

        Reservation reservation = new Reservation();
        reservation.setNumberOfPersons(10);
        reservation.setStart(fromDate);
        reservation.setEnd(toDate);

        DayAvailability a1 = new DayAvailability(fromDate, 90, DEFAULT_MAX_AVAILABILITY);
        DayAvailability a2 = new DayAvailability(middleDate, 90, DEFAULT_MAX_AVAILABILITY);
        List<DayAvailability> availabilities = Arrays.asList(a1, a2);

        Mockito.when(availabilityRepository.findAllById(List.of(fromDate, middleDate))).thenReturn(new ArrayList<>());
        Mockito.when(availabilityRepository.saveAll(availabilities)).thenReturn(availabilities);

        List<DayAvailability> updatedAvalabilities = availabilityService.updateDayAvailability(reservation);

        assertNotNull(updatedAvalabilities);
        assertEquals(2, updatedAvalabilities.size());
        assertEquals(90, updatedAvalabilities.get(0).getAvailability());
        assertEquals(90, updatedAvalabilities.get(1).getAvailability());

    }

    @Test
    public void testUpdateDayAvailabilityExistingRecords() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate middleDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);

        Reservation reservation = new Reservation();
        reservation.setNumberOfPersons(10);
        reservation.setStart(fromDate);
        reservation.setEnd(toDate);

        DayAvailability a1 = new DayAvailability(fromDate, 75, DEFAULT_MAX_AVAILABILITY);
        DayAvailability updatedA1 = new DayAvailability(fromDate, 65, DEFAULT_MAX_AVAILABILITY);
        DayAvailability a2 = new DayAvailability(middleDate, 90, DEFAULT_MAX_AVAILABILITY);
        List<DayAvailability> existingAvailabilities = new ArrayList<>();
        existingAvailabilities.add(a1);
        List<DayAvailability> availabilities = Arrays.asList(updatedA1, a2);

        Mockito.when(availabilityRepository.findAllById(List.of(fromDate, middleDate))).thenReturn(existingAvailabilities);
        Mockito.when(availabilityRepository.saveAll(availabilities)).thenReturn(availabilities);

        List<DayAvailability> updatedAvalabilities = availabilityService.updateDayAvailability(reservation);

        assertNotNull(updatedAvalabilities);
        assertEquals(2, updatedAvalabilities.size());
        assertEquals(65, updatedAvalabilities.get(0).getAvailability());
        assertEquals(90, updatedAvalabilities.get(1).getAvailability());
    }

    @Test(expected = NoAvailabilityForDateException.class)
    public void testUpdateDayAvailabilityNoAvailability() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate middleDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);

        Reservation reservation = new Reservation();
        reservation.setNumberOfPersons(10);
        reservation.setStart(fromDate);
        reservation.setEnd(toDate);

        DayAvailability a1 = new DayAvailability(fromDate, 5, DEFAULT_MAX_AVAILABILITY);
        DayAvailability updatedA1 = new DayAvailability(fromDate, 65, DEFAULT_MAX_AVAILABILITY);
        DayAvailability a2 = new DayAvailability(middleDate, 90, DEFAULT_MAX_AVAILABILITY);
        List<DayAvailability> existingAvailabilities = new ArrayList<>();
        existingAvailabilities.add(a1);
        List<DayAvailability> availabilities = Arrays.asList(updatedA1, a2);

        Mockito.when(availabilityRepository.findAllById(List.of(fromDate, middleDate))).thenReturn(existingAvailabilities);
        Mockito.when(availabilityRepository.saveAll(availabilities)).thenReturn(availabilities);

        availabilityService.updateDayAvailability(reservation);
    }

}
