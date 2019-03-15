package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.dto.Occupancy;
import com.upgrade.islandreservationsapi.exception.NoAvailabilityForDateException;
import com.upgrade.islandreservationsapi.model.Dates;
import com.upgrade.islandreservationsapi.model.Reservation;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

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
    private ReservationRepository reservationRepository;

    @MockBean
    private ConfigurationService configurationService;

    @MockBean
    private DatesRepository datesRepository;

    @Autowired
    private ReservationService reservationService;

    @Before
    public void init() {
        Mockito.when(configurationService.getMaxAvailability()).thenReturn(100);
        List<Dates> dates = new ArrayList<>();
        LocalDate.now().datesUntil(LocalDate.now().plusDays(30)).forEach(d -> dates.add(new Dates(d)));
        Mockito.when(datesRepository.findAllById(any())).thenReturn(dates);
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

        Occupancy o1 = new Occupancy(fromDate, 10);
        Occupancy o2 = new Occupancy(toDate, 10);

        Mockito.when(reservationRepository.save(reservation)).thenReturn(reservationWithId);
        Mockito.when(reservationRepository.calculateOccupancyByRange(reservation.getStart(), reservation.getEnd()))
                .thenReturn(List.of(o1, o2));

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

        Occupancy o1 = new Occupancy(fromDate, 25);
        Occupancy o2 = new Occupancy(toDate, 10);


        Mockito.when(reservationRepository.save(reservation)).thenReturn(reservationWithId);
        Mockito.when(reservationRepository.calculateOccupancyByRange(reservation.getStart(), reservation.getEnd()))
                .thenReturn(List.of(o1, o2));

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

        Occupancy o1 = new Occupancy(fromDate, 12);

        Mockito.when(reservationRepository.findAndLockById(101)).thenReturn(Optional.of(existingReservation));
        Mockito.when(reservationRepository.calculateOccupancyExcludeReservation(fromDate.datesUntil(toDate).collect(Collectors.toList()), existingReservation.getId()))
                .thenReturn(List.of(o1));
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
        List<Occupancy> occupancies = Collections.emptyList();

        Mockito.when(reservationRepository.findAndLockById(101)).thenReturn(Optional.of(existingReservation));
        Mockito.when(reservationRepository.calculateOccupancyExcludeReservation(fromDate.datesUntil(toDate).collect(Collectors.toList()), existingReservation.getId()))
                .thenReturn(occupancies);
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

        Mockito.when(reservationRepository.findAndLockById(101)).thenReturn(Optional.of(existingReservation));
        Mockito.when(reservationRepository.save(newReservation)).thenReturn(newReservation);

        Reservation result = reservationService.updateReservation(newReservation);
        assertEquals("Oliver", result.getLastName());
        assertEquals((Integer) 101, result.getId());
    }


}
