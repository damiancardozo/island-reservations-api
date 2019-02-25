package com.upgrade.islandreservationsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upgrade.islandreservationsapi.exception.NoAvailabilityForDateException;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.service.ConfigurationService;
import com.upgrade.islandreservationsapi.service.ReservationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ConfigurationService configurationService;

    @Before
    public void initialize() throws NoAvailabilityForDateException {
        given(configurationService.getMinAheadDays()).willReturn(1);
        given(configurationService.getMaxAheadDays()).willReturn(30);
        given(configurationService.getMaxReservation()).willReturn(3);

        Reservation reservationReturned = new Reservation();
        reservationReturned.setId(91);
        given(reservationService.createReservation(new Reservation())).willReturn(reservationReturned);
    }

    @Test
    public void testCreateInvalidReservation() throws Exception {

        Reservation reservation = new Reservation();
        reservation.setEmail("test@email.com");
        reservation.setFistName("John");
        reservation.setLastName("Oliver");
        reservation.setNumberOfPersons(3);
        reservation.setStart(LocalDate.now());
        reservation.setEnd(LocalDate.now().plusDays(2));
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(reservation);

        mvc.perform(post("/reservation")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("ASCII")
            .content(jsonBody))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void testCreateReservation() throws Exception {

        Reservation reservation = new Reservation();
        reservation.setEmail("test@email.com");
        reservation.setFistName("John");
        reservation.setLastName("Oliver");
        reservation.setNumberOfPersons(3);
        reservation.setStart(LocalDate.now().plusDays(2));
        reservation.setEnd(LocalDate.now().plusDays(4));
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(reservation);

        mvc.perform(post("/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("ASCII")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk());
    }

}
