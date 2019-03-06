package com.upgrade.islandreservationsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upgrade.islandreservationsapi.dto.ReservationDTO;
import com.upgrade.islandreservationsapi.exception.ReservationNotFoundException;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.service.ConfigurationService;
import com.upgrade.islandreservationsapi.service.ReservationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

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
    public void initialize() throws Exception {
        given(configurationService.getMinAheadDays()).willReturn(1);
        given(configurationService.getMaxAheadDays()).willReturn(30);
        given(configurationService.getMaxReservation()).willReturn(3);

        Reservation reservationReturned = new Reservation();
        reservationReturned.setId(91);
        given(reservationService.createReservation(new Reservation())).willReturn(reservationReturned);
    }

    @Test
    public void testGetReservation() throws Exception {
        Reservation reservation = new Reservation("John", "Oliver", "johnoliver@gmail.com", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 5);
        reservation.setId(101);
        given(reservationService.getReservation(101)).willReturn(reservation);

        mvc.perform(get("/v1/reservations/101")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("johnoliver@gmail.com")))
                .andExpect(jsonPath("$.status", is("Active")));
    }

    @Test
    public void testGetReservationNotFound() throws Exception {
        given(reservationService.getReservation(101)).willThrow(new ReservationNotFoundException(101));

        mvc.perform(get("/v1/reservations/101")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateInvalidReservation() throws Exception {

        Reservation reservation = new Reservation("John", "Oliver", "johnoliver@gmail.com",
                LocalDate.now(), LocalDate.now().plusDays(2), 3);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        mvc.perform(post("/v1/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(jsonBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[*].path",
                    containsInAnyOrder("start", "start")))
            .andExpect(jsonPath("$.fieldErrors[*].message",
                    containsInAnyOrder("must be a future date",
                            "start date must be at least 1 day(s) in the future.")));
    }

    @Test
    public void testCreateReservation() throws Exception {

        Reservation reservation = new Reservation("John", "Oliver", "johnoliver@gmail.com",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 3);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        mvc.perform(post("/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void updateReservation() throws Exception {
        Reservation reservation = new Reservation("John", "Oliver", "johnoliver@gmail.com",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 3);
        reservation.setId(101);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        given(reservationService.updateReservation(reservation)).willReturn(reservation);

        mvc.perform(put("/v1/reservations/101")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.status", is("Active")))
                .andExpect(jsonPath("numberOfPersons", is(3)));
    }

    @Test
    public void updateInvalidReservation() throws Exception {
        Reservation reservation = new Reservation("John", "Oliver", "johnoliver@gmail.com",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), 3);
        reservation.setEmail("test@email.com");
        reservation.setId(101);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        given(reservationService.updateReservation(reservation)).willReturn(reservation);

        mvc.perform(put("/v1/reservations/101")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors[*].path",
                        containsInAnyOrder("end")))
                .andExpect(jsonPath("$.fieldErrors[*].message",
                        containsInAnyOrder("max duration is 3 day(s).")));
    }

    @Test
    public void testCancelReservation() throws Exception {

        Reservation reservation = new Reservation("John", "Oliver", "johnoliver@gmail.com", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 5);
        reservation.setStatus(Reservation.Status.CANCELLED);
        given(reservationService.cancelReservation(91)).willReturn(reservation);

        mvc.perform(delete("/v1/reservations/91")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Cancelled")));
    }

}
