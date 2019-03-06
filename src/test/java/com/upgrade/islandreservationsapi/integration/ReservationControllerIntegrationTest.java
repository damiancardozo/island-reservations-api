package com.upgrade.islandreservationsapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upgrade.islandreservationsapi.dto.ReservationCreated;
import com.upgrade.islandreservationsapi.dto.ReservationDTO;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.DayAvailabilityRepository;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
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

    @Autowired
    private DayAvailabilityRepository availabilityRepository;
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
        assertFalse(created.isEmpty());
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
        logger.info(availabilityRepository.findAll().toString());

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
        logger.info(availabilityRepository.findAll().toString());

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

        runMultithreaded(() -> {
            try {
                mvc.perform(put("/v1/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody));
            } catch (Exception e) {
                System.out.println("exception in put " + e);
            }
        }, 5);

        logger.info(reservationRepository.findAll().toString());
        logger.info(availabilityRepository.findAll().toString());

        Optional<DayAvailability> availabilityOpt1 = availabilityRepository.findById(LocalDate.now().plusDays(1));
        assertTrue(availabilityOpt1.isPresent());
        assertEquals(85, availabilityOpt1.get().getAvailability());
        Optional<DayAvailability> availabilityOpt2 = availabilityRepository.findById(LocalDate.now().plusDays(2));
        assertTrue(availabilityOpt2.isPresent());
        assertEquals(85, availabilityOpt2.get().getAvailability());
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

        runMultithreaded(() -> {
            try {
                mvc.perform(put("/v1/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody));
            } catch (Exception e) {
                System.out.println("exception in put " + e);
            }
        }, 5);

        logger.info(reservationRepository.findAll().toString());
        logger.info(availabilityRepository.findAll().toString());

        Optional<DayAvailability> availabilityOpt1 = availabilityRepository.findById(LocalDate.now().plusDays(1));
        assertTrue(availabilityOpt1.isPresent());
        assertEquals(100, availabilityOpt1.get().getAvailability());
        Optional<DayAvailability> availabilityOpt2 = availabilityRepository.findById(LocalDate.now().plusDays(2));
        assertTrue(availabilityOpt2.isPresent());
        assertEquals(85, availabilityOpt2.get().getAvailability());
        Optional<DayAvailability> availabilityOpt3 = availabilityRepository.findById(LocalDate.now().plusDays(3));
        assertTrue(availabilityOpt3.isPresent());
        assertEquals(85, availabilityOpt3.get().getAvailability());
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
