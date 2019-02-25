package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.NoAvailabilityForDateException;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.DayAvailabilityRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
public class ReservationServiceTest {


    @TestConfiguration
    static class ReservationServiceTestContextConfiguration {

        @Bean
        public ReservationServiceImpl reservationService() {
            return new ReservationServiceImpl();
        }
    }

    @MockBean
    private DayAvailabilityRepository availabilityRepository;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private ConfigurationService configurationService;

    @Autowired
    private ReservationService reservationService;

    @Before
    public void init() {
        Mockito.when(configurationService.getMaxAvailability()).thenReturn(100);
    }

    @Test
    public void testCreateNoPreviousReservations() throws NoAvailabilityForDateException {

        LocalDate fromDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);
        Reservation reservation = new Reservation("John", "Oliver", "johnoliver@email.com",
                fromDate, toDate, 10);
        Reservation reservationWithId = new Reservation("John", "Doe", "johndoe@email.com",
                fromDate, toDate, 10);
        reservationWithId.setId(100);

        Mockito.when(reservationRepository.save(reservation)).thenReturn(reservationWithId);
        Mockito.when(availabilityRepository.findById(fromDate)).thenReturn(Optional.empty());
        Mockito.when(availabilityRepository.findById(toDate)).thenReturn(Optional.empty());

        DayAvailability availability1 = new DayAvailability(fromDate, 90, 100);
        DayAvailability availability2 = new DayAvailability(toDate, 90, 100);

        Mockito.when(availabilityRepository.save(availability1)).thenReturn(availability1);
        Mockito.when(availabilityRepository.save(availability2)).thenReturn(availability2);

        Reservation createdReservation = reservationService.createReservation(reservation);

        assertNotNull(createdReservation);
        assertEquals(reservationWithId, createdReservation);
    }

    @Test
    public void testCreateReservation() throws NoAvailabilityForDateException {

        LocalDate fromDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);
        Reservation reservation = new Reservation("John", "Doe", "johndoe@email.com",
                fromDate, toDate, 10);
        Reservation reservationWithId = new Reservation("John", "Doe", "johndoe@email.com",
                fromDate, toDate, 10);
        reservationWithId.setId(100);

        DayAvailability availability1 = new DayAvailability(fromDate, 85, 100);
        DayAvailability availability2 = new DayAvailability(toDate, 90, 100);

        Mockito.when(reservationRepository.save(reservation)).thenReturn(reservationWithId);
        Mockito.when(availabilityRepository.findById(fromDate)).thenReturn(Optional.of(availability1));
        Mockito.when(availabilityRepository.findById(toDate)).thenReturn(Optional.empty());

        DayAvailability availabilityUpdated = new DayAvailability(fromDate, 75, 100);

        Mockito.when(availabilityRepository.save(availabilityUpdated)).thenReturn(availabilityUpdated);
        Mockito.when(availabilityRepository.save(availability2)).thenReturn(availability2);

        Reservation createdReservation = reservationService.createReservation(reservation);

        assertNotNull(createdReservation);
        assertEquals(reservationWithId, createdReservation);
    }

    @Test
    public void testUpdateReservationNumberChange() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);
        Reservation existingReservation = new Reservation("John", "Doe", "johndoe@email.com",
                fromDate, toDate, 10);
        existingReservation.setId(101);

        Reservation newReservation = new Reservation("John", "Doe", "johndoe@email.com",
                fromDate, toDate, 12);
        newReservation.setId(101);

        DayAvailability a1 = new DayAvailability(fromDate, 90, 100);
        DayAvailability a2 = new DayAvailability(toDate, 90, 100);
        List<LocalDate> dates = List.of(fromDate, toDate);
        List<DayAvailability> availabilities = List.of(a1, a2);

        Mockito.when(reservationRepository.findById(101)).thenReturn(Optional.of(existingReservation));
        Mockito.when(availabilityRepository.findAllById(dates)).thenReturn(availabilities);
        Mockito.when(availabilityRepository.saveAll(availabilities)).thenReturn(availabilities);
        Mockito.when(reservationRepository.save(existingReservation)).thenReturn(newReservation);

        reservationService.updateReservation(newReservation);

        assertEquals((Integer) 12, newReservation.getNumberOfPersons());
    }

    @Test
    public void testUpdateReservationNumberAndDatesChange() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);
        Reservation existingReservation = new Reservation("John", "Doe", "johndoe@email.com",
                fromDate, toDate, 10);
        existingReservation.setId(101);

        LocalDate newFromDate = LocalDate.now().plusDays(1);
        LocalDate newToDate = LocalDate.now().plusDays(4);

        Reservation newReservation = new Reservation("John", "Doe", "johndoe@email.com",
                newFromDate, newToDate, 12);
        newReservation.setId(101);

        DayAvailability a1 = new DayAvailability(fromDate, 90, 100);
        DayAvailability a2 = new DayAvailability(toDate, 90, 100);
        List<LocalDate> dates = List.of(fromDate, toDate);
        List<DayAvailability> availabilities = List.of(a1, a2);

        DayAvailability a3 = new DayAvailability(newFromDate, 88, 100);
        DayAvailability a4 = new DayAvailability(fromDate, 88, 100);
        DayAvailability a5 = new DayAvailability(toDate, 88, 100);
        DayAvailability a6 = new DayAvailability(newToDate, 88, 100);
        List<DayAvailability> newAvailabilities = List.of(a3, a4, a5, a6);

        Mockito.when(reservationRepository.findById(101)).thenReturn(Optional.of(existingReservation));
        Mockito.when(availabilityRepository.findAllById(dates)).thenReturn(availabilities);
        Mockito.when(availabilityRepository.saveAll(availabilities)).thenReturn(availabilities);
        Mockito.when(availabilityRepository.saveAll(newAvailabilities)).thenReturn(newAvailabilities);
        Mockito.when(reservationRepository.save(existingReservation)).thenReturn(newReservation);

        newReservation = reservationService.updateReservation(newReservation);

        assertEquals((Integer) 12, newReservation.getNumberOfPersons());
        assertEquals(newFromDate, newReservation.getStart());
        assertEquals(newToDate, newReservation.getEnd());
    }

    @Test
    public void testUpdateReservationNameChange() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(3);
        Reservation existingReservation = new Reservation("John", "Doe", "johndoe@email.com",
                fromDate, toDate, 12);
        existingReservation.setId(101);

        Reservation newReservation = new Reservation("John", "Oliver", "johndoe@email.com",
                fromDate, toDate, 12);
        newReservation.setId(101);

        Mockito.when(reservationRepository.findById(101)).thenReturn(Optional.of(existingReservation));
        Mockito.when(reservationRepository.save(newReservation)).thenReturn(newReservation);

        Reservation result = reservationService.updateReservation(newReservation);
        assertEquals("Oliver", result.getLastName());
        assertEquals((Integer) 101, result.getId());
    }


}
