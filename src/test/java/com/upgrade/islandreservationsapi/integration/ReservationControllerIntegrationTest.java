package com.upgrade.islandreservationsapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upgrade.islandreservationsapi.dto.ReservationCreated;
import com.upgrade.islandreservationsapi.dto.ReservationDTO;
import com.upgrade.islandreservationsapi.helper.ResponseCollector;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReservationRepository reservationRepository;

    private final Logger logger = LogManager.getLogger(ReservationControllerIntegrationTest.class);

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
        assertTrue(created.isPresent());
        assertEquals(2, created.get().getReservationDays().size());
    }

    @Test
    public void testConcurrentCreate() throws Exception {
        Reservation reservation = new Reservation("Luke", "Warm", "lukew@gmail.com",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 15);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        runMultithreaded(() -> {
            try {
                mvc.perform(post("/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                logger.error("exception in post", e);
            }
        }, 5);

        assertEquals(6, reservationRepository.count());
    }

    @Test
    public void testConcurrentCreateNoAvailabilityForAll() throws Exception {
        Reservation reservation = new Reservation("Luke", "Warm", "lukew@gmail.com",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 30);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        runMultithreaded(() -> {
            try {
                mvc.perform(post("/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody));
            } catch (Exception e) {
                logger.error("exception in post ", e);
            }
        }, 5);

        assertEquals(4, reservationRepository.count());
    }

    @Test
    public void testConcurrentCreateWithExistingAvailabilityRecords() throws Exception {
        Reservation reservation = new Reservation("Luke", "Warm", "lukew@gmail.com",
                LocalDate.now().plusDays(6), LocalDate.now().plusDays(8), 30);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        runMultithreaded(() -> {
            try {
                mvc.perform(post("/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody));
            } catch (Exception e) {
                logger.error("exception in post ", e);
            }
        }, 5);

        logger.info(reservationRepository.findAll().toString());
        assertEquals(4, reservationRepository.count());

    }

    @Test
    public void testUpdateReservationInvalidStart() throws Exception {
        Reservation reservation = new Reservation("John", "Doe", "jdoe@hotmail.com",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 15);
        reservation = reservationRepository.saveAndFlush(reservation);

        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);

        dto.setStart(LocalDate.now());
        String jsonBody = mapper.writeValueAsString(dto);

        mvc.perform(put("/v1/reservations/" + reservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors[*].path",
                        containsInAnyOrder("start")))
                .andExpect(jsonPath("$.fieldErrors[*].message",
                        containsInAnyOrder("start date must be at least 1 day(s) in the future.")));

        Optional<Reservation> reservationInDb = reservationRepository.findById(reservation.getId());
        assertTrue(reservationInDb.isPresent());
        assertEquals(LocalDate.now().plusDays(2), reservationInDb.get().getEnd());
    }

    @Test
    public void testUpdateReservation() throws Exception {
        Reservation reservation = new Reservation("John", "Doe", "jdoe@hotmail.com",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 15);
        reservation.addReservationDays();
        reservation = reservationRepository.saveAndFlush(reservation);

        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);

        dto.setStart(LocalDate.now().plusDays(3));
        dto.setEnd(LocalDate.now().plusDays(5));
        dto.setNumberOfPersons(20);
        String jsonBody = mapper.writeValueAsString(dto);

        mvc.perform(put("/v1/reservations/" + reservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk());

        Optional<Reservation> reservationInDbOpt = reservationRepository.findById(reservation.getId());
        assertTrue(reservationInDbOpt.isPresent());
        Reservation reservationInDb = reservationInDbOpt.get();
        assertEquals(LocalDate.now().plusDays(3), reservationInDb.getStart());
        assertEquals(LocalDate.now().plusDays(5), reservationInDb.getEnd());
        assertEquals(2, reservationInDb.getReservationDays().size());
        assertTrue(reservationInDb.getReservationDays().stream().anyMatch(rd -> rd.getId().getDate().equals(LocalDate.now().plusDays(3))));
        assertTrue(reservationInDb.getReservationDays().stream().anyMatch(rd -> rd.getId().getDate().equals(LocalDate.now().plusDays(4))));
    }

    @Test
    public void testUpdateNoAvailability() throws Exception {
        Reservation reservation = new Reservation("John", "Doe", "jdoe@hotmail.com",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 15);
        reservation.addReservationDays();
        reservation = reservationRepository.saveAndFlush(reservation);

        Reservation reservation2 = new Reservation("Luke", "Warm", "luke@gmail.com",
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(8), 40);
        reservation2.addReservationDays();
        reservationRepository.saveAndFlush(reservation2);

        Reservation reservation3 = new Reservation("Luke", "Warm", "luke@gmail.com",
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), 35);
        reservation3.addReservationDays();
        reservationRepository.saveAndFlush(reservation3);

        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);

        dto.setStart(LocalDate.now().plusDays(7));
        dto.setEnd(LocalDate.now().plusDays(10));
        dto.setNumberOfPersons(40);
        String jsonBody = mapper.writeValueAsString(dto);

        mvc.perform(put("/v1/reservations/" + reservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateReservationWhenStarted() throws Exception {
        Reservation reservation = new Reservation("John", "Doe", "jdoe@hotmail.com",
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), 15);
        reservation = reservationRepository.saveAndFlush(reservation);

        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);

        dto.setEnd(LocalDate.now().plusDays(2));
        String jsonBody = mapper.writeValueAsString(dto);

        mvc.perform(put("/v1/reservations/" + reservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk());

        Optional<Reservation> reservationInDb = reservationRepository.findById(reservation.getId());
        assertTrue(reservationInDb.isPresent());
        assertEquals(LocalDate.now().plusDays(2), reservationInDb.get().getEnd());
    }

    @Test
    public void testUpdateReservationStartDateWhenStarted() throws Exception {
        Reservation reservation = new Reservation("John", "Doe", "jdoe@hotmail.com",
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), 15);
        reservation = reservationRepository.saveAndFlush(reservation);

        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);

        dto.setStart(LocalDate.now());
        String jsonBody = mapper.writeValueAsString(dto);

        mvc.perform(put("/v1/reservations/" + reservation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors[*].path",
                        containsInAnyOrder("start")))
                .andExpect(jsonPath("$.fieldErrors[*].message",
                        containsInAnyOrder("start date can't be updated for a reservation that already started")));
    }

    @Test
    public void testConcurrentCreateWithSomeExistingAvailabilityRecords() throws Exception {
        Reservation reservation = new Reservation("Luke", "Warm", "lukew@gmail.com",
                LocalDate.now().plusDays(8), LocalDate.now().plusDays(10), 30);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        runMultithreaded(() -> {
            try {
                mvc.perform(post("/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody));
            } catch (Exception e) {
                System.out.println("exception in post " + e);
            }
        }, 5);

        logger.info(reservationRepository.findAll().toString());
        assertEquals(4, reservationRepository.count());

    }

    @Test
    public void testConcurrentUpdateSameDates() throws Exception {
        Reservation reservation = new Reservation("John", "Oliver", "johno@gmail.com",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 15);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        dto.setNumberOfPersons(25);
        String jsonBody2 = mapper.writeValueAsString(dto);

        ResponseCollector collector = new ResponseCollector();

        Runnable r1 = () -> {
            try {
                MvcResult result = mvc.perform(put("/v1/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody))
                        .andReturn();
                collector.addResponseBody(result.getResponse().getContentAsString());
                collector.addResponseStatus(result.getResponse().getStatus());
            } catch (Exception e) {
                System.out.println("exception in put " + e);
            }
        };
        Runnable r2 = () -> {
            try {
                MvcResult result = mvc.perform(put("/v1/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody2))
                        .andReturn();
                collector.addResponseBody(result.getResponse().getContentAsString());
                collector.addResponseStatus(result.getResponse().getStatus());
            } catch (Exception e) {
                System.out.println("exception in put " + e);
            }
        };

        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        Thread t3 = new Thread(r1);
        Thread t4 = new Thread(r2);
        List<Thread> threadList = List.of(t1, t2, t3, t4);

        for( Thread t :  threadList) {
            t.start();
        }

        for( Thread t :  threadList) {
            t.join();
        }

        logger.info(reservationRepository.findAll().toString());

        assertEquals(1, collector.countStatusByCode(HttpStatus.OK));
        assertEquals(3, collector.countStatusByCode(HttpStatus.CONFLICT));
    }

    @Test
    public void testConcurrentUpdateDifferentDates() throws Exception {
        Reservation reservation = new Reservation("John", "Oliver", "johno@gmail.com",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 15);
        ModelMapper modelMapper = new ModelMapper();
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        String jsonBody = mapper.writeValueAsString(dto);

        dto.setNumberOfPersons(20);
        String jsonBody2 = mapper.writeValueAsString(dto);

        ResponseCollector collector = new ResponseCollector();

        Runnable r1 = () -> {
            try {
                MvcResult result = mvc.perform(put("/v1/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody))
                        .andReturn();
                collector.addResponseBody(result.getResponse().getContentAsString());
                collector.addResponseStatus(result.getResponse().getStatus());
            } catch (Exception e) {
                System.out.println("exception in put " + e);
            }
        };
        Runnable r2 = () -> {
            try {
                MvcResult result = mvc.perform(put("/v1/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody2))
                        .andReturn();
                collector.addResponseBody(result.getResponse().getContentAsString());
                collector.addResponseStatus(result.getResponse().getStatus());
            } catch (Exception e) {
                System.out.println("exception in put " + e);
            }
        };

        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        Thread t3 = new Thread(r1);
        Thread t4 = new Thread(r2);
        List<Thread> threadList = List.of(t1, t2, t3, t4);

        for( Thread t :  threadList) {
            t.start();
        }

        for( Thread t :  threadList) {
            t.join();
        }

        logger.info(reservationRepository.findAll().toString());

        assertEquals(1, collector.countStatusByCode(HttpStatus.OK));
        assertEquals(3, collector.countStatusByCode(HttpStatus.CONFLICT));

    }

    public static void runMultithreaded(Runnable  runnable, int threadCount) throws InterruptedException {
        List<Thread> threadList = new LinkedList<>();

        for(int i = 0 ; i < threadCount; i++) {
            threadList.add(new Thread(runnable));
        }

        for( Thread t :  threadList) {
            t.start();
        }

        for( Thread t :  threadList) {
            t.join();
        }
    }
}
