package com.upgrade.islandreservationsapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upgrade.islandreservationsapi.dto.ReservationCreated;
import com.upgrade.islandreservationsapi.dto.ReservationDTO;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    public void testGetReservation() throws Exception {
        mvc.perform(get("/v1/reservations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("johno@gmail.com")))
                .andExpect(jsonPath("$.status", is("Active")));
    }

    @Test
    public void testGetNonExistingReservation() throws Exception {
        mvc.perform(get("/v1/reservations/8729")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Reservation not found")));
    }

    @Test
    public void testGetReservationInvalidId() throws Exception {
        mvc.perform(get("/v1/reservations/zzz")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andDo(print());
                //.andExpect(jsonPath("$.message", is("Reservation not found")));
    }

    @Test
    public void testCreateReservation() throws Exception {
        Reservation reservation = new Reservation("Luke", "Warm", "lukew@gmail.com",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 3);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);
        final ReservationCreated rc = new ReservationCreated();
        mvc.perform(post("/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(mvcResult -> {
                    ObjectMapper jsonMapper = new ObjectMapper();
                    String json = mvcResult.getResponse().getContentAsString();
                    ReservationCreated response = jsonMapper.readValue(json, ReservationCreated.class);
                    rc.setId(response.getId());
                });

        Optional<Reservation> created = reservationRepository.findById(rc.getId());
        assertFalse(created.isEmpty());
    }
}
